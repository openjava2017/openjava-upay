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
import org.openjava.upay.trade.domain.RefundTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.service.IPasswordService;
import org.openjava.upay.trade.service.IRefundTransactionService;
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;
import org.openjava.upay.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service("refundTransactionService")
public class RefundTransactionServiceImpl implements IRefundTransactionService
{
    private static final Logger LOG = LoggerFactory.getLogger(RefundTransactionServiceImpl.class);

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IFundTransactionDao fundTransactionDao;

    @Resource
    private IFundStreamEngine fundStreamEngine;

    @Resource
    private IPasswordService passwordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionId refund(Merchant merchant, RefundTransaction transaction) throws Exception
    {
        Date when = new Date();
        checkRefundTransaction(transaction);

        LOG.info("Handle fund transaction refund request, serialNo:{} amount:{}",
                transaction.getSerialNo(), transaction.getAmount());
        FundAccount fromAccount = fundAccountDao.findFundAccountById(transaction.getAccountId());
        if (fromAccount == null || fromAccount.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (fromAccount.getStatus() != AccountStatus.NORMAL) {
            throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        // 验证原收款方(现付款方)账号密码
        passwordService.checkPaymentPermission(fromAccount, transaction.getPassword());
        FundTransaction fundTransaction = fundTransactionDao.findFundTransactionByNo(transaction.getSerialNo());
        if (fundTransaction == null) {
            throw new FundTransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        if (fundTransaction.getType() != TransactionType.TRADE) {
            throw new FundTransactionException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
        if (fundTransaction.getStatus() != TransactionStatus.STATUS_COMPLETED) {
            throw new FundTransactionException(ErrorCode.INVALID_TRANSACTION_STATUS);
        }
        //验证原交易单收款方资金账号与参数中是否一致
        AssertUtils.isTrue(fundTransaction.getTargetId() == transaction.getAccountId(),
            "Invalid account Id");
        //退款金额不能大于交易金额
        if (transaction.getAmount() > fundTransaction.getAmount()) {
            throw new FundTransactionException(ErrorCode.REFUND_FUNDS_EXCEED);
        }
        // 验证原付款方(现收款方)账户状态
        FundAccount toAccount = fundAccountDao.findFundAccountById(fundTransaction.getFromId());
        if (toAccount == null || toAccount.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        //更新资金事务表的交易金额, 初始金额max_amount不做修改
        Long newAmount = fundTransaction.getAmount() - transaction.getAmount();
        int result = fundTransactionDao.compareAndSetAmount(fundTransaction.getId(), newAmount,
                fundTransaction.getAmount(), when);
        if (result <= 0) {
            throw new FundTransactionException(ErrorCode.FUND_TRANSACTION_FAILED);
        }

        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(KeyGeneratorManager.SequenceKey.FUND_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        FundTransaction refundTransaction = new FundTransaction();
        refundTransaction.setId(keyGenerator.nextId());
        refundTransaction.setMerchantId(merchant.getId());
        refundTransaction.setSerialNo(serialKeyGenerator.nextSerialNo(String.valueOf(TransactionType.REFUND.getCode())));
        refundTransaction.setTargetNo(transaction.getSerialNo());
        refundTransaction.setType(TransactionType.REFUND);
        refundTransaction.setFromId(fromAccount.getId());
        refundTransaction.setFromName(fromAccount.getName());
        refundTransaction.setTargetId(toAccount.getId());
        refundTransaction.setTargetName(toAccount.getName());
        refundTransaction.setPipeline(fundTransaction.getPipeline());
        refundTransaction.setMaxAmount(transaction.getAmount());
        refundTransaction.setAmount(transaction.getAmount());
        refundTransaction.setStatus(TransactionStatus.STATUS_COMPLETED);
        refundTransaction.setDescription(transaction.getDescription());
        refundTransaction.setCreatedTime(when);
        refundTransaction.setModifiedTime(null);
        fundTransactionDao.createFundTransaction(refundTransaction);

        // 处理买家账户-账户支出
        FundActivity fromActivity = new FundActivity();
        fromActivity.setTransactionId(refundTransaction.getId());
        fromActivity.setPipeline(refundTransaction.getPipeline());
        fromActivity.setAction(Action.OUTGO);
        fromActivity.setType(FundType.FUND);
        fromActivity.setAmount(refundTransaction.getAmount());
        fromActivity.setDescription(refundTransaction.getType().getName() + Action.OUTGO.getName());
        fundStreamEngine.submit(refundTransaction.getFromId(), fromActivity);

        // 处理卖家账户-账户收入 费用支出
        FundActivity toActivity = new FundActivity();
        toActivity.setTransactionId(refundTransaction.getId());
        toActivity.setPipeline(refundTransaction.getPipeline());
        toActivity.setAction(Action.INCOME);
        toActivity.setType(FundType.FUND);
        toActivity.setAmount(refundTransaction.getAmount());
        toActivity.setDescription(refundTransaction.getType().getName() + Action.INCOME.getName());
        fundStreamEngine.submit(refundTransaction.getTargetId(), toActivity);

        TransactionId transactionId = new TransactionId();
        transactionId.setId(refundTransaction.getId());
        transactionId.setSerialNo(refundTransaction.getSerialNo());
        return transactionId;
    }

    private void checkRefundTransaction(RefundTransaction transaction)
    {
        AssertUtils.notNull(transaction.getSerialNo(), "Argument missed: serialNo");
        AssertUtils.notNull(transaction.getAccountId(), "Argument missed: accountId");
        AssertUtils.notNull(transaction.getAmount(), "Argument missed: amount");
        AssertUtils.isTrue(transaction.getAmount() > 0, "Invalid transaction amount");
        AssertUtils.notNull(transaction.getPassword(), "Argument missed: password");
    }
}
