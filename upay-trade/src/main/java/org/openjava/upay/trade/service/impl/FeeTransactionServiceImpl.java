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
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.dao.IFundTransactionDao;
import org.openjava.upay.trade.domain.Fee;
import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.model.TransactionFee;
import org.openjava.upay.trade.service.IFundTransactionService;
import org.openjava.upay.trade.service.XATransactionService;
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;
import org.openjava.upay.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeeTransactionServiceImpl implements XATransactionService
{
    private static Logger LOG = LoggerFactory.getLogger(FeeTransactionServiceImpl.class);

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IFundTransactionDao fundTransactionDao;

    @Resource
    private IFundStreamEngine fundStreamEngine;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IFundTransactionService fundTransactionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction beginTransaction(Transaction transaction) throws Exception
    {
        // Arguments check
        Date when = new Date();
        Merchant merchant = transaction.getMerchant();

        FundAccount account = null;
        if (transaction.getPipeline() == Pipeline.ACCOUNT) {
            if (transaction.getToId() == null) {
                LOG.error("Argument missed: Account Id");
                throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
            }
            account = fundAccountDao.findFundAccountById(transaction.getToId());
            if (account == null || account.getStatus() == AccountStatus.LOGOFF) {
                throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
            }
            if (account.getStatus() != AccountStatus.NORMAL) {
                throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
            }
        }

        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            for (Fee fee : transaction.getFees()) {
                if (fee.getPipeline() == transaction.getPipeline()) {
                    LOG.error("Fee pipeline != transaction pipeline");
                    throw new FundTransactionException(ErrorCode.INVALID_ARGUMENT);
                }
            }
        } else {
            LOG.error("Argument missed: fees");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }

        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(KeyGeneratorManager.SequenceKey.FUND_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        transaction.setId(keyGenerator.nextId());
        transaction.setSerialNo(serialKeyGenerator.nextSerialNo(
                String.valueOf(getTransactionType().getCode()), TransactionType.class.getSimpleName()));

        long accountId = transaction.getPipeline() == Pipeline.ACCOUNT ? account.getId() : 0;
        String accountName = transaction.getPipeline() == Pipeline.ACCOUNT ? account.getName() : null;
        FundTransaction fundTransaction = new FundTransaction();
        fundTransaction.setId(transaction.getId());
        fundTransaction.setMerchantId(merchant.getId());
        fundTransaction.setSerialNo(transaction.getSerialNo());
        fundTransaction.setType(getTransactionType());
        fundTransaction.setToId(accountId);
        fundTransaction.setToName(accountName);
        fundTransaction.setPipeline(transaction.getPipeline());
        fundTransaction.setAmount(transaction.getAmount());
        fundTransaction.setStatus(TransactionStatus.STATUS_APPLY);
        fundTransaction.setDescription(transaction.getDescription());
        fundTransaction.setCreatedTime(when);
        fundTransaction.setModifiedTime(null);
        fundTransactionDao.createFundTransaction(fundTransaction);

        for (Fee fee : transaction.getFees()) {
            TransactionFee transactionFee = new TransactionFee();
            transactionFee.setTransactionId(fundTransaction.getId());
            transactionFee.setPipeline(fee.getPipeline());
            transactionFee.setType(fee.getType());
            transactionFee.setAmount(fee.getAmount());
            transactionFee.setCreatedTime(when);
            fundTransactionDao.createTransactionFee(transactionFee);
        }

        return transaction;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Transaction commitTransaction(Transaction transaction) throws Exception
    {
        Date when = new Date();
        if (transaction.getId() == null) {
            LOG.error("Argument missed: transaction Id");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }
        FundTransaction fundTransaction = fundTransactionDao.findFundTransactionById(transaction.getId());
        if (fundTransaction == null) {
            throw new FundTransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        if (fundTransaction.getStatus() == TransactionStatus.STATUS_COMPLETED) {
            return transaction;
        }
        if (fundTransaction.getStatus() != TransactionStatus.STATUS_APPLY) {
            throw new FundTransactionException(ErrorCode.INVALID_TRANSACTION_STATUS);
        }
        if (fundTransaction.getMerchantId() != transaction.getMerchant().getId()) {
            throw new FundTransactionException(ErrorCode.INVALID_MERCHANT);
        }

        // 缴费使用账户支付需要验证账户状态和密码
        if (fundTransaction.getPipeline() == Pipeline.ACCOUNT) {
            FundAccount account = fundAccountDao.findFundAccountById(fundTransaction.getToId());
            if (account.getStatus() != AccountStatus.NORMAL) {
                throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
            }
            fundTransactionService.checkPaymentPermission(account, transaction.getPassword());
        }

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
            fundStreamEngine.submit(transaction.getMerchant().getAccountId(), activities.toArray(new FundActivity[0]));
        }

        // 处理个人账户缴费扣款, fee pipeline = transaction pipeline
        if (transaction.getPipeline() == Pipeline.ACCOUNT) {
            List<FundActivity> activities = new ArrayList<>();
            for (TransactionFee fee : fees) {
                FundActivity feeActivity = new FundActivity();
                feeActivity.setTransactionId(fee.getTransactionId());
                feeActivity.setPipeline(fee.getPipeline());
                feeActivity.setAction(Action.OUTGO);
                feeActivity.setAmount(fee.getAmount());
                feeActivity.setDescription(fee.getType().getName() + Action.OUTGO.getName());
                activities.add(feeActivity);
            }
            fundStreamEngine.submit(transaction.getToId(), activities.toArray(new FundActivity[0]));
        }

        //更新事务状态
        int result = fundTransactionDao.compareAndSetStatus(fundTransaction.getId(), TransactionStatus.STATUS_COMPLETED,
                TransactionStatus.STATUS_APPLY, when);
        if (result <= 0) {
            throw new FundTransactionException(ErrorCode.DATA_CONCURRENT_MODIFY);
        }

        return transaction;
    }

    @Override
    public Transaction rollBackTransaction(Transaction transaction) throws Exception
    {
        return null;
    }

    @Override
    public TransactionType getTransactionType()
    {
        return TransactionType.MAKE_FEE;
    }
}
