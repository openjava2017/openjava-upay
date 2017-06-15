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
import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.model.TransactionFee;
import org.openjava.upay.trade.service.IFeeTransactionService;
import org.openjava.upay.trade.service.IFundTransactionService;
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

@CallableComponent({"rollback"})
@Service("feeTransactionService")
public class FeeTransactionServiceImpl extends CallableServiceComponent implements IFeeTransactionService
{
    private static Logger LOG = LoggerFactory.getLogger(FeeTransactionServiceImpl.class);

    private static final String COMPONENT_ID = "payment.service.fee";

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
    public TransactionId submit(ServiceRequest<Transaction> request) throws Exception
    {
        // Arguments check
        Date when = new Date();
        Transaction transaction = request.getData();
        Merchant merchant = request.getContext().getMerchant();

        FundAccount account = null;
        if (transaction.getPipeline() == Pipeline.ACCOUNT) {
            if (transaction.getAccountId() == null) {
                LOG.error("Argument missed: Account Id");
                throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
            }
            account = fundAccountDao.findFundAccountById(transaction.getAccountId());
            if (account == null || account.getStatus() == AccountStatus.LOGOFF) {
                throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
            }
            if (account.getStatus() != AccountStatus.NORMAL) {
                throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
            }

            // 缴费使用账户支付需要验证账户状态和密码
            fundTransactionService.checkPaymentPermission(account, transaction.getPassword());
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

        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(SequenceKey.FUND_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        if (transaction.getSerialNo() == null) {
            transaction.setSerialNo(serialKeyGenerator.nextSerialNo(
                    String.valueOf(TransactionType.WITHDRAW.getCode()), TransactionType.class.getSimpleName()));
        }

        long accountId = transaction.getPipeline() == Pipeline.ACCOUNT ? account.getId() : 0;
        String accountName = transaction.getPipeline() == Pipeline.ACCOUNT ? account.getName() : null;
        FundTransaction fundTransaction = new FundTransaction();
        fundTransaction.setId(keyGenerator.nextId());
        fundTransaction.setMerchantId(merchant.getId());
        fundTransaction.setSerialNo(transaction.getSerialNo());
        fundTransaction.setType(TransactionType.MAKE_FEE);
        fundTransaction.setToId(accountId);
        fundTransaction.setToName(accountName);
        fundTransaction.setPipeline(transaction.getPipeline());
        fundTransaction.setAmount(transaction.getAmount());
        fundTransaction.setStatus(TransactionStatus.STATUS_COMPLETED);
        fundTransaction.setDescription(transaction.getDescription());
        fundTransaction.setCreatedTime(when);
        fundTransaction.setModifiedTime(null);
        fundTransactionDao.createFundTransaction(fundTransaction);

        List<TransactionFee> fees = new ArrayList<>();
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
            fundStreamEngine.submit(transaction.getAccountId(), activities.toArray(new FundActivity[0]));
        }

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
