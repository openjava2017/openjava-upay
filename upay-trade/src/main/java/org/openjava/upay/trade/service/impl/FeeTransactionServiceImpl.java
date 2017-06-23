package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.dao.IFundAccountDao;
import org.openjava.upay.core.domain.FundActivity;
import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IFundStreamEngine;
import org.openjava.upay.core.type.AccountStatus;
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
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;
import org.openjava.upay.trade.util.TransactionServiceHelper;
import org.openjava.upay.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("feeTransactionService")
public class FeeTransactionServiceImpl implements IFeeTransactionService
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
    public TransactionId makeFee(Merchant merchant, Transaction transaction) throws Exception
    {
        // Arguments check
        Date when = new Date();

        if (ObjectUtils.isEmpty(transaction.getFees())) {
            LOG.error("Argument missed: fees");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }
        if (!TransactionServiceHelper.validFeePipeline(transaction.getFees())) {
            LOG.error("Only CASH or ACCOUNT pipeline supported for transaction fee");
            throw new FundTransactionException(ErrorCode.INVALID_ARGUMENT);
        }
        for (Fee fee : transaction.getFees()) {
            if (fee.getPipeline() == transaction.getPipeline()) {
                LOG.error("Fee pipeline != transaction pipeline");
                throw new FundTransactionException(ErrorCode.INVALID_ARGUMENT);
            }
        }

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

        List<TransactionFee> fees = TransactionServiceHelper.wrapTransactionFees(
            fundTransaction.getId(), transaction.getFees(), when);
        for (TransactionFee fee : fees) {
            fundTransactionDao.createTransactionFee(fee);
        }

        // 处理商户账户-费用收入
        List<FundActivity> merActivities = TransactionServiceHelper.wrapFeeActivitiesForMer(fees);
        if (ObjectUtils.isNotEmpty(merActivities)) {
            fundStreamEngine.submit(merchant.getAccountId(), merActivities.toArray(new FundActivity[0]));
        }

        // 处理个人账户缴费扣款, fee pipeline = transaction pipeline
        if (transaction.getPipeline() == Pipeline.ACCOUNT) {
            List<FundActivity> accountActivities = TransactionServiceHelper.wrapFeeActivitiesForAccount(fees);
            if (ObjectUtils.isNotEmpty(accountActivities)) {
                fundStreamEngine.submit(fundTransaction.getToId(), accountActivities.toArray(new FundActivity[0]));
            }
        }

        TransactionId transactionId = new TransactionId();
        transactionId.setId(fundTransaction.getId());
        transactionId.setSerialNo(fundTransaction.getSerialNo());
        return transactionId;
    }
}
