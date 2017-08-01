package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.dao.IFundAccountDao;
import org.openjava.upay.core.domain.FundActivity;
import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IFundStreamEngine;
import org.openjava.upay.core.type.AccountStatus;
import org.openjava.upay.core.type.Action;
import org.openjava.upay.core.type.FundType;
import org.openjava.upay.shared.sequence.IKeyGenerator;
import org.openjava.upay.shared.sequence.ISerialKeyGenerator;
import org.openjava.upay.shared.sequence.KeyGeneratorManager;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.dao.IFundTransactionDao;
import org.openjava.upay.trade.domain.FlushTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.service.IFlushTransactionService;
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;
import org.openjava.upay.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service("flushTransactionService")
public class FlushTransactionServiceImpl implements IFlushTransactionService
{
    private static final Logger LOG = LoggerFactory.getLogger(FlushTransactionServiceImpl.class);

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IFundTransactionDao fundTransactionDao;

    @Resource
    private IFundStreamEngine fundStreamEngine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionId flush(Merchant merchant, FlushTransaction transaction) throws Exception
    {
        // Arguments check
        Date when = new Date();
        checkFlushTransaction(transaction);

        LOG.info("Handle fund flush request, serialNo:{} amount:{}",
                transaction.getSerialNo(), transaction.getAmount());
        FundTransaction fundTransaction = fundTransactionDao.findFundTransactionByNo(transaction.getSerialNo());
        if (fundTransaction == null) {
            throw new FundTransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        if (fundTransaction.getType() != TransactionType.DEPOSIT && fundTransaction.getType() != TransactionType.WITHDRAW) {
            throw new FundTransactionException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
        if (fundTransaction.getStatus() != TransactionStatus.STATUS_COMPLETED) {
            throw new FundTransactionException(ErrorCode.INVALID_TRANSACTION_STATUS);
        }
        FundAccount account = fundAccountDao.findFundAccountById(fundTransaction.getTargetId());
        if (account == null || account.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        //冲正金额不能大于交易金额
        if (transaction.getAmount() > fundTransaction.getAmount()) {
            throw new FundTransactionException(ErrorCode.FLUSH_FUNDS_EXCEED);
        }
        //更新资金事务表的交易金额, 初始金额max_amount不做修改
        Long newAmount = fundTransaction.getAmount() - transaction.getAmount();
        int result = fundTransactionDao.compareAndSetAmount(fundTransaction.getId(), newAmount,
                fundTransaction.getAmount(), when);
        if (result <= 0) {
            throw new FundTransactionException(ErrorCode.FUND_TRANSACTION_FAILED);
        }

        //创建冲正交易
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(KeyGeneratorManager.SequenceKey.FUND_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        FundTransaction flushTransaction = new FundTransaction();
        flushTransaction.setId(keyGenerator.nextId());
        flushTransaction.setMerchantId(merchant.getId());
        flushTransaction.setSerialNo(serialKeyGenerator.nextSerialNo(String.valueOf(TransactionType.FLUSH.getCode())));
        flushTransaction.setTargetNo(transaction.getSerialNo());
        flushTransaction.setType(TransactionType.FLUSH);
        flushTransaction.setTargetId(fundTransaction.getTargetId());
        flushTransaction.setTargetName(fundTransaction.getTargetName());
        flushTransaction.setPipeline(fundTransaction.getPipeline());
        flushTransaction.setMaxAmount(transaction.getAmount());
        flushTransaction.setAmount(transaction.getAmount());
        flushTransaction.setStatus(TransactionStatus.STATUS_COMPLETED);
        flushTransaction.setDescription(transaction.getDescription());
        flushTransaction.setCreatedTime(when);
        flushTransaction.setModifiedTime(null);
        fundTransactionDao.createFundTransaction(flushTransaction);

        // 处理账户资金: 充值冲正账户资金扣减, 提现冲正账户资金增加
        Action action = fundTransaction.getType() == TransactionType.DEPOSIT ? Action.OUTGO : Action.INCOME;
        FundActivity activity = new FundActivity();
        activity.setTransactionId(flushTransaction.getId());
        activity.setPipeline(flushTransaction.getPipeline());
        activity.setAction(action);
        activity.setType(FundType.FUND);
        activity.setAmount(flushTransaction.getAmount());
        activity.setDescription(fundTransaction.getType().getName() + flushTransaction.getType().getName());
        fundStreamEngine.submit(flushTransaction.getTargetId(), activity);

        TransactionId transactionId = new TransactionId();
        transactionId.setId(flushTransaction.getId());
        transactionId.setSerialNo(flushTransaction.getSerialNo());
        return transactionId;
    }

    private void checkFlushTransaction(FlushTransaction transaction)
    {
        AssertUtils.notEmpty(transaction.getSerialNo(), "Argument missed: serialNo");
        AssertUtils.notNull(transaction.getAmount(), "Argument missed: amount");
        AssertUtils.isTrue(transaction.getAmount() > 0, "Invalid transaction amount");
    }
}
