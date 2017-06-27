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
import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.model.TransactionFee;
import org.openjava.upay.trade.service.IDepositTransactionService;
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;
import org.openjava.upay.trade.util.TransactionServiceHelper;
import org.openjava.upay.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("depositTransactionService")
public class DepositTransactionServiceImpl implements IDepositTransactionService
{
    private static final Logger LOG = LoggerFactory.getLogger(DepositTransactionServiceImpl.class);

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
    public TransactionId deposit(Merchant merchant, Transaction transaction) throws Exception
    {
        // Arguments check
        Date when = new Date();

        // 充值只支持现金和POS
        if (transaction.getPipeline() != Pipeline.CASH && transaction.getPipeline() != Pipeline.POS) {
            LOG.error("Only CASH or POS pipeline supported for account deposit");
            throw new FundTransactionException(ErrorCode.INVALID_ARGUMENT);
        }

        if (transaction.getAccountId() == null) {
            LOG.error("Argument missed: Account Id");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            LOG.error("Illegal Argument: amount != null && amount > 0");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }
        if (!TransactionServiceHelper.validTransactionFees(transaction.getFees())) {
            LOG.error("Invalid fee pipeline or amount: ACCOUNT/CASH and amount > 0");
            throw new FundTransactionException(ErrorCode.INVALID_ARGUMENT);
        }
        FundAccount account = fundAccountDao.findFundAccountById(transaction.getAccountId());
        if (account == null || account.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(SequenceKey.FUND_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        if (transaction.getSerialNo() == null) {
            transaction.setSerialNo(serialKeyGenerator.nextSerialNo(
                String.valueOf(TransactionType.DEPOSIT.getCode()), TransactionType.class.getSimpleName()));
        }

        FundTransaction fundTransaction = new FundTransaction();
        fundTransaction.setId(keyGenerator.nextId());
        fundTransaction.setMerchantId(merchant.getId());
        fundTransaction.setSerialNo(transaction.getSerialNo());
        fundTransaction.setType(TransactionType.DEPOSIT);
        fundTransaction.setToId(transaction.getAccountId());
        fundTransaction.setToName(account.getName());
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

        // 处理个人账户-账户充值 费用支出
        List<FundActivity> activities = new ArrayList<>();
        FundActivity activity = new FundActivity();
        activity.setTransactionId(fundTransaction.getId());
        activity.setPipeline(fundTransaction.getPipeline());
        activity.setAction(Action.INCOME);
        activity.setAmount(fundTransaction.getAmount());
        activity.setDescription(fundTransaction.getType().getName());
        activities.add(activity);
        if (ObjectUtils.isNotEmpty(fees)) {
            TransactionServiceHelper.wrapFeeActivitiesForAccount(activities, fees);
        }
        fundStreamEngine.submit(fundTransaction.getToId(), activities.toArray(new FundActivity[0]));

        TransactionId transactionId = new TransactionId();
        transactionId.setId(fundTransaction.getId());
        transactionId.setSerialNo(fundTransaction.getSerialNo());
        return transactionId;
    }
}
