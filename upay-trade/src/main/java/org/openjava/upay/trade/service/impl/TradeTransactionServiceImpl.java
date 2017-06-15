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
import org.openjava.upay.trade.domain.*;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.model.TransactionFee;
import org.openjava.upay.trade.service.IFundTransactionService;
import org.openjava.upay.trade.service.ITradeTransactionService;
import org.openjava.upay.trade.support.CallableComponent;
import org.openjava.upay.trade.support.CallableServiceComponent;
import org.openjava.upay.trade.support.ServiceRequest;
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;
import org.openjava.upay.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CallableComponent
@Service("tradeTransactionService")
public class TradeTransactionServiceImpl extends CallableServiceComponent implements ITradeTransactionService
{
    private static Logger LOG = LoggerFactory.getLogger(TradeTransactionServiceImpl.class);

    private static final String COMPONENT_ID = "payment.service.trade";

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
    public TransactionId prepare(ServiceRequest<TradePrepareTransaction> request) throws Exception
    {
        // Arguments check
        Date when = new Date();
        TradePrepareTransaction transaction = request.getData();
        Merchant merchant = request.getContext().getMerchant();

        // 交易只支持账户支付
        if (transaction.getPipeline() != Pipeline.ACCOUNT) {
            LOG.error("Only ACCOUNT pipeline supported for account trade");
            throw new FundTransactionException(ErrorCode.INVALID_ARGUMENT);
        }

        // 交易费用只能使用账户支付
        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            for (Fee fee : transaction.getFees()) {
                if (fee.getPipeline() != Pipeline.ACCOUNT) {
                    LOG.error("Only CASH or ACCOUNT pipeline supported for transaction fee");
                    throw new FundTransactionException(ErrorCode.INVALID_ARGUMENT);
                }
            }
        }

        if (transaction.getFromId() == null) {
            LOG.error("Argument missed: From Account Id");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }
        FundAccount fromAccount = fundAccountDao.findFundAccountById(transaction.getFromId());
        if (fromAccount == null || fromAccount.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (fromAccount.getStatus() != AccountStatus.NORMAL) {
            throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        if (transaction.getToId() == null) {
            LOG.error("Argument missed: To Account Id");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }
        FundAccount toAccount = fundAccountDao.findFundAccountById(transaction.getToId());
        if (toAccount == null || toAccount.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

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
        fundTransaction.setStatus(TransactionStatus.STATUS_APPLY);
        fundTransaction.setDescription(transaction.getDescription());
        fundTransaction.setCreatedTime(when);
        fundTransaction.setModifiedTime(null);
        fundTransactionDao.createFundTransaction(fundTransaction);

        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            for (Fee fee : transaction.getFees()) {
                TransactionFee transactionFee = new TransactionFee();
                transactionFee.setTransactionId(fundTransaction.getId());
                transactionFee.setPipeline(fee.getPipeline());
                transactionFee.setType(fee.getType());
                transactionFee.setAmount(fee.getAmount());
                transactionFee.setCreatedTime(when);
                fundTransactionDao.createTransactionFee(transactionFee);
            }
        }

        TransactionId transactionId = new TransactionId();
        transactionId.setId(fundTransaction.getId());
        transactionId.setSerialNo(fundTransaction.getSerialNo());
        return transactionId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionId commit(ServiceRequest<TradeCommitTransaction> request) throws Exception
    {
        Date when = new Date();
        TradeCommitTransaction transaction = request.getData();
        Merchant merchant = request.getContext().getMerchant();

        if (transaction.getId() == null) {
            LOG.error("Argument missed: transaction Id");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }
        FundTransaction fundTransaction = fundTransactionDao.findFundTransactionById(transaction.getId());
        if (fundTransaction == null) {
            throw new FundTransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        if (fundTransaction.getStatus() == TransactionStatus.STATUS_COMPLETED) {
            TransactionId transactionId = new TransactionId();
            transactionId.setId(fundTransaction.getId());
            transactionId.setSerialNo(fundTransaction.getSerialNo());
            return transactionId;
        }
        if (fundTransaction.getStatus() != TransactionStatus.STATUS_APPLY) {
            throw new FundTransactionException(ErrorCode.INVALID_TRANSACTION_STATUS);
        }
        if (fundTransaction.getMerchantId() != merchant.getId()) {
            throw new FundTransactionException(ErrorCode.INVALID_MERCHANT);
        }

        // 验证买家账户状态和密码
        FundAccount fromAccount = fundAccountDao.findFundAccountById(fundTransaction.getFromId());
        if (fromAccount.getStatus() != AccountStatus.NORMAL) {
            throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }
        fundTransactionService.checkPaymentPermission(fromAccount, transaction.getPassword());

        // 处理商户账户-费用收入
        List<TransactionFee> fees = fundTransactionDao.findFeesByTransactionId(fundTransaction.getId());
        if (ObjectUtils.isNotEmpty(fees)) {
            List<FundActivity> activities = new ArrayList<>();
            for (TransactionFee fee : fees) {
                FundActivity activity = new FundActivity();
                activity.setTransactionId(fee.getTransactionId());
                activity.setPipeline(fee.getPipeline());
                activity.setAction(Action.INCOME);
                activity.setAmount(fee.getAmount());
                activity.setDescription(fee.getType().getName() + Action.INCOME.getName());
                activities.add(activity);
            }
            fundStreamEngine.submit(merchant.getAccountId(), activities.toArray(new FundActivity[0]));
        }

        // 处理买家账户-账户支出
        FundActivity fromActivity = new FundActivity();
        fromActivity.setTransactionId(fundTransaction.getId());
        fromActivity.setPipeline(fundTransaction.getPipeline());
        fromActivity.setAction(Action.OUTGO);
        fromActivity.setAmount(fundTransaction.getAmount());
        fromActivity.setDescription(fundTransaction.getType().getName());
        fundStreamEngine.submit(fundTransaction.getFromId(), fromActivity);

        // 处理卖家账户-账户收入 费用支出
        List<FundActivity> activities = new ArrayList<>();
        FundActivity toActivity = new FundActivity();
        toActivity.setTransactionId(fundTransaction.getId());
        toActivity.setPipeline(fundTransaction.getPipeline());
        toActivity.setAction(Action.INCOME);
        toActivity.setAmount(fundTransaction.getAmount());
        toActivity.setDescription(fundTransaction.getType().getName());
        activities.add(toActivity);
        if (ObjectUtils.isNotEmpty(fees)) {
            for (TransactionFee fee : fees) {
                if (fee.getPipeline() == Pipeline.ACCOUNT) { //费用支出通过账户扣减方式
                    FundActivity feeActivity = new FundActivity();
                    feeActivity.setTransactionId(fee.getTransactionId());
                    feeActivity.setPipeline(fee.getPipeline());
                    feeActivity.setAction(Action.OUTGO);
                    feeActivity.setAmount(fee.getAmount());
                    feeActivity.setDescription(fee.getType().getName() + Action.OUTGO.getName());
                    activities.add(feeActivity);
                }
            }
        }
        fundStreamEngine.submit(fundTransaction.getToId(), activities.toArray(new FundActivity[0]));

        //更新事务状态
        int result = fundTransactionDao.compareAndSetStatus(fundTransaction.getId(), TransactionStatus.STATUS_COMPLETED,
                TransactionStatus.STATUS_APPLY, when);
        if (result <= 0) {
            throw new FundTransactionException(ErrorCode.DATA_CONCURRENT_MODIFY);
        }

        TransactionId transactionId = new TransactionId();
        transactionId.setId(fundTransaction.getId());
        transactionId.setSerialNo(fundTransaction.getSerialNo());
        return transactionId;
    }

    @Override
    public TransactionId submit(ServiceRequest<TradeTransaction> request) throws Exception
    {
        // Arguments check
        Date when = new Date();
        TradeTransaction transaction = request.getData();
        Merchant merchant = request.getContext().getMerchant();

        // 交易只支持账户支付
        if (transaction.getPipeline() != Pipeline.ACCOUNT) {
            LOG.error("Only ACCOUNT pipeline supported for account trade");
            throw new FundTransactionException(ErrorCode.INVALID_ARGUMENT);
        }

        // 交易费用只能使用账户支付
        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            for (Fee fee : transaction.getFees()) {
                if (fee.getPipeline() != Pipeline.ACCOUNT) {
                    LOG.error("Only CASH or ACCOUNT pipeline supported for transaction fee");
                    throw new FundTransactionException(ErrorCode.INVALID_ARGUMENT);
                }
            }
        }

        if (transaction.getFromId() == null) {
            LOG.error("Argument missed: From Account Id");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }
        FundAccount fromAccount = fundAccountDao.findFundAccountById(transaction.getFromId());
        if (fromAccount == null || fromAccount.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (fromAccount.getStatus() != AccountStatus.NORMAL) {
            throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        if (transaction.getToId() == null) {
            LOG.error("Argument missed: To Account Id");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
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

        List<TransactionFee> fees = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            for (Fee fee : transaction.getFees()) {
                TransactionFee transactionFee = new TransactionFee();
                transactionFee.setTransactionId(fundTransaction.getId());
                transactionFee.setPipeline(fee.getPipeline());
                transactionFee.setType(fee.getType());
                transactionFee.setAmount(fee.getAmount());
                transactionFee.setCreatedTime(when);
                fees.add(transactionFee);
                fundTransactionDao.createTransactionFee(transactionFee);
            }
        }

        // 处理商户账户-费用收入
        if (ObjectUtils.isNotEmpty(fees)) {
            List<FundActivity> activities = new ArrayList<>();
            for (TransactionFee fee : fees) {
                FundActivity activity = new FundActivity();
                activity.setTransactionId(fee.getTransactionId());
                activity.setPipeline(fee.getPipeline());
                activity.setAction(Action.INCOME);
                activity.setAmount(fee.getAmount());
                activity.setDescription(fee.getType().getName() + Action.INCOME.getName());
                activities.add(activity);
            }
            fundStreamEngine.submit(merchant.getAccountId(), activities.toArray(new FundActivity[0]));
        }

        // 处理买家账户-账户支出
        FundActivity fromActivity = new FundActivity();
        fromActivity.setTransactionId(fundTransaction.getId());
        fromActivity.setPipeline(fundTransaction.getPipeline());
        fromActivity.setAction(Action.OUTGO);
        fromActivity.setAmount(fundTransaction.getAmount());
        fromActivity.setDescription(fundTransaction.getType().getName());
        fundStreamEngine.submit(fundTransaction.getFromId(), fromActivity);

        // 处理卖家账户-账户收入 费用支出
        List<FundActivity> activities = new ArrayList<>();
        FundActivity toActivity = new FundActivity();
        toActivity.setTransactionId(fundTransaction.getId());
        toActivity.setPipeline(fundTransaction.getPipeline());
        toActivity.setAction(Action.INCOME);
        toActivity.setAmount(fundTransaction.getAmount());
        toActivity.setDescription(fundTransaction.getType().getName());
        activities.add(toActivity);
        if (ObjectUtils.isNotEmpty(fees)) {
            for (TransactionFee fee : fees) {
                if (fee.getPipeline() == Pipeline.ACCOUNT) { //费用支出通过账户扣减方式
                    FundActivity feeActivity = new FundActivity();
                    feeActivity.setTransactionId(fee.getTransactionId());
                    feeActivity.setPipeline(fee.getPipeline());
                    feeActivity.setAction(Action.OUTGO);
                    feeActivity.setAmount(fee.getAmount());
                    feeActivity.setDescription(fee.getType().getName() + Action.OUTGO.getName());
                    activities.add(feeActivity);
                }
            }
        }
        fundStreamEngine.submit(fundTransaction.getToId(), activities.toArray(new FundActivity[0]));

        TransactionId transactionId = new TransactionId();
        transactionId.setId(fundTransaction.getId());
        transactionId.setSerialNo(fundTransaction.getSerialNo());
        return transactionId;
    }

    @Override
    public String componentId()
    {
        return COMPONENT_ID;
    }
}
