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
import org.openjava.upay.core.type.StatementType;
import org.openjava.upay.shared.sequence.IKeyGenerator;
import org.openjava.upay.shared.sequence.ISerialKeyGenerator;
import org.openjava.upay.shared.sequence.KeyGeneratorManager;
import org.openjava.upay.shared.sequence.KeyGeneratorManager.SequenceKey;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.dao.IFundTransactionDao;
import org.openjava.upay.trade.domain.Fee;
import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.model.TransactionFee;
import org.openjava.upay.trade.service.IFundTransactionService;
import org.openjava.upay.trade.service.IWithdrawTransactionService;
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

@Service("withdrawTransactionService")
public class WithdrawTransactionServiceImpl implements IWithdrawTransactionService
{
    private static Logger LOG = LoggerFactory.getLogger(WithdrawTransactionServiceImpl.class);

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
    public TransactionId withdraw(Merchant merchant, Transaction transaction) throws Exception
    {
        // Arguments check
        Date when = new Date();
        checkWithdrawTransaction(transaction);

        LOG.info("Handle fund account withdraw request, accountId:{} amount:{}",
            transaction.getAccountId(), transaction.getAmount());
        FundAccount account = fundAccountDao.findFundAccountById(transaction.getAccountId());
        if (account == null || account.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (account.getStatus() != AccountStatus.NORMAL) {
            throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        // 验证买家账户状态和密码
        fundTransactionService.checkPaymentPermission(account, transaction.getPassword());
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(SequenceKey.FUND_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        if (transaction.getSerialNo() == null) {
            transaction.setSerialNo(serialKeyGenerator.nextSerialNo(String.valueOf(TransactionType.WITHDRAW.getCode())));
        }

        FundTransaction fundTransaction = new FundTransaction();
        fundTransaction.setId(keyGenerator.nextId());
        fundTransaction.setMerchantId(merchant.getId());
        fundTransaction.setSerialNo(transaction.getSerialNo());
        fundTransaction.setType(TransactionType.WITHDRAW);
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

        // 处理个人账户-账户提现 费用支出
        List<FundActivity> activities = new ArrayList<>();
        FundActivity activity = new FundActivity();
        activity.setTransactionId(fundTransaction.getId());
        activity.setPipeline(fundTransaction.getPipeline());
        activity.setAction(Action.OUTGO);
        activity.setType(StatementType.FUND);
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

    private void checkWithdrawTransaction(Transaction transaction)
    {
        AssertUtils.notNull(transaction.getAccountId(), "Argument missed: accountId");
        AssertUtils.notNull(transaction.getAmount(), "Argument missed: amount");
        AssertUtils.isTrue(transaction.getAmount() > 0, "Invalid transaction amount");
        // 提现只支持现金
        AssertUtils.isTrue(transaction.getPipeline() == Pipeline.CASH,
            "Invalid transaction pipeline");
        AssertUtils.notNull(transaction.getPassword(), "Argument missed: password");

        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            for (Fee fee : transaction.getFees()) {
                AssertUtils.notNull(fee.getAmount(), "Argument missed: fee amount");
                AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
                AssertUtils.isTrue(fee.getPipeline() == Pipeline.ACCOUNT ||
                    fee.getPipeline() == Pipeline.CASH, "Invalid fee pipeline");
            }
        }
    }
}
