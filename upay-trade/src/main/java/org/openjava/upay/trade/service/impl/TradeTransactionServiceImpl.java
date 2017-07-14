package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.dao.IFundAccountDao;
import org.openjava.upay.core.domain.FundActivity;
import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IFundStreamEngine;
import org.openjava.upay.core.type.AccountStatus;
import org.openjava.upay.core.type.Action;
import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.shared.sequence.IKeyGenerator;
import org.openjava.upay.shared.sequence.ISerialKeyGenerator;
import org.openjava.upay.shared.sequence.KeyGeneratorManager;
import org.openjava.upay.shared.sequence.KeyGeneratorManager.SequenceKey;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.dao.IFundTransactionDao;
import org.openjava.upay.trade.domain.Fee;
import org.openjava.upay.trade.domain.TradeTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.model.TransactionFee;
import org.openjava.upay.trade.service.IFundTransactionService;
import org.openjava.upay.trade.service.ITradeTransactionService;
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;
import org.openjava.upay.trade.util.TransactionServiceHelper;
import org.openjava.upay.util.AssertUtils;
import org.openjava.upay.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("tradeTransactionService")
public class TradeTransactionServiceImpl implements ITradeTransactionService
{
    private static Logger LOG = LoggerFactory.getLogger(TradeTransactionServiceImpl.class);

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IFundTransactionDao fundTransactionDao;

    @Resource
    private IFundStreamEngine fundStreamEngine;

    @Resource
    private IFundTransactionService fundTransactionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionId trade(Merchant merchant, TradeTransaction transaction) throws Exception
    {
        // Arguments check
        Date when = new Date();
        checkTradeTransaction(transaction);

        LOG.info("Handle fund account trade request, fromId:{} toId:{} amount:{}", transaction.getFromId(),
            transaction.getToId(), transaction.getAmount());
        FundAccount fromAccount = fundAccountDao.findFundAccountById(transaction.getFromId());
        if (fromAccount == null || fromAccount.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (fromAccount.getStatus() != AccountStatus.NORMAL) {
            throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        FundAccount toAccount = fundAccountDao.findFundAccountById(transaction.getToId());
        if (toAccount == null || toAccount.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        // 验证买家账户状态和密码
        fundTransactionService.checkPaymentPermission(fromAccount, transaction.getPassword());
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(SequenceKey.FUND_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        if (transaction.getSerialNo() == null) {
            transaction.setSerialNo(serialKeyGenerator.nextSerialNo(
                String.valueOf(TransactionType.TRADE.getCode()), TransactionType.class.getSimpleName()));
        }

        FundTransaction fundTransaction = new FundTransaction();
        fundTransaction.setId(keyGenerator.nextId());
        fundTransaction.setMerchantId(merchant.getId());
        fundTransaction.setSerialNo(transaction.getSerialNo());
        fundTransaction.setType(TransactionType.TRADE);
        fundTransaction.setFromId(transaction.getFromId());
        fundTransaction.setFromName(fromAccount.getName());
        fundTransaction.setToId(transaction.getToId());
        fundTransaction.setToName(toAccount.getName());
        fundTransaction.setPipeline(transaction.getPipeline());
        fundTransaction.setAmount(transaction.getAmount());
        fundTransaction.setStatus(TransactionStatus.STATUS_COMPLETED);
        fundTransaction.setDescription(transaction.getDescription());
        fundTransaction.setCreatedTime(when);
        fundTransaction.setModifiedTime(null);
        fundTransactionDao.createFundTransaction(fundTransaction);

        List<TransactionFee> fees = null;
        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            fees = TransactionServiceHelper.wrapTransactionFees(fundTransaction.getId(), transaction.getFees(), when);
            for (TransactionFee fee : fees) {
                fundTransactionDao.createTransactionFee(fee);
            }
        }

        // 处理商户账户-费用收入
        if (ObjectUtils.isNotEmpty(fees)) {
            List<FundActivity> activities = TransactionServiceHelper.wrapFeeActivitiesForMer(fees);
            fundStreamEngine.submit(merchant.getAccountId(), activities.toArray(new FundActivity[0]));
        }

        // 处理买家账户-账户支出
        FundActivity fromActivity = new FundActivity();
        fromActivity.setTransactionId(fundTransaction.getId());
        fromActivity.setPipeline(fundTransaction.getPipeline());
        fromActivity.setAction(Action.OUTGO);
        fromActivity.setAmount(fundTransaction.getAmount());
        fromActivity.setDescription(fundTransaction.getType().getName() + Action.OUTGO.getName());
        fundStreamEngine.submit(fundTransaction.getFromId(), fromActivity);

        // 处理卖家账户-账户收入 费用支出
        List<FundActivity> activities = new ArrayList<>();
        FundActivity toActivity = new FundActivity();
        toActivity.setTransactionId(fundTransaction.getId());
        toActivity.setPipeline(fundTransaction.getPipeline());
        toActivity.setAction(Action.INCOME);
        toActivity.setAmount(fundTransaction.getAmount());
        toActivity.setDescription(fundTransaction.getType().getName() + Action.INCOME.getName());
        activities.add(toActivity);
        if (ObjectUtils.isNotEmpty(fees)) {
            TransactionServiceHelper.wrapFeeActivitiesForAccount(activities, fees);
        }
        fundStreamEngine.submit(fundTransaction.getToId(), activities.toArray(new FundActivity[0]));

        TransactionId transactionId = new TransactionId();
        transactionId.setId(fundTransaction.getId());
        transactionId.setSerialNo(fundTransaction.getSerialNo());
        return transactionId;
    }

    private void checkTradeTransaction(TradeTransaction transaction)
    {
        AssertUtils.notNull(transaction.getFromId(), "Argument missed: fromId");
        AssertUtils.notNull(transaction.getToId(), "Argument missed: toId");
        AssertUtils.notNull(transaction.getAmount(), "Argument missed: amount");
        AssertUtils.notNull(transaction.getPassword(), "Argument missed: password");
        // 交易只支持账户支付
        AssertUtils.isTrue(transaction.getPipeline() == Pipeline.ACCOUNT,
            "Invalid transaction pipeline");
        AssertUtils.isTrue(transaction.getAmount() > 0, "Invalid transaction amount");

        // 交易费用只能使用账户支付
        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            for (Fee fee : transaction.getFees()) {
                AssertUtils.isTrue(fee.getPipeline() == Pipeline.ACCOUNT, "Invalid fee pipeline");
                AssertUtils.notNull(fee.getAmount(), "Argument missed: fee amount");
                AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
            }
        }
    }
}
