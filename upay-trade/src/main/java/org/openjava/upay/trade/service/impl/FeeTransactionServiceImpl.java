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
import org.openjava.upay.trade.service.IPasswordService;
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
    private IPasswordService passwordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionId payFees(Merchant merchant, Transaction transaction) throws Exception
    {
        // Arguments check
        Date when = new Date();
        checkFeeTransaction(transaction);

        LOG.info("Handle fund account fee request, accountId:{} amount:{}",
            transaction.getAccountId(), transaction.getAmount());
        FundAccount account = fundAccountDao.findFundAccountById(transaction.getAccountId());
        if (account == null || account.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (account.getStatus() != AccountStatus.NORMAL) {
            throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }
        if (transaction.getPipeline() == Pipeline.ACCOUNT) {
            // 缴费使用账户支付需要验证账户状态和密码
            passwordService.checkPaymentPermission(account, transaction.getPassword());
        }

        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(SequenceKey.FUND_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        if (transaction.getSerialNo() == null) {
            transaction.setSerialNo(serialKeyGenerator.nextSerialNo(String.valueOf(TransactionType.PAY_FEE.getCode())));
        }

        FundTransaction fundTransaction = new FundTransaction();
        fundTransaction.setId(keyGenerator.nextId());
        fundTransaction.setMerchantId(merchant.getId());
        fundTransaction.setSerialNo(transaction.getSerialNo());
        fundTransaction.setType(TransactionType.PAY_FEE);
        fundTransaction.setTargetId(account.getId());
        fundTransaction.setTargetName(account.getName());
        fundTransaction.setPipeline(transaction.getPipeline());
        fundTransaction.setMaxAmount(transaction.getAmount());
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

        // 处理个人账户缴费扣款, fee pipeline = transaction pipeline
        if (transaction.getPipeline() == Pipeline.ACCOUNT && ObjectUtils.isNotEmpty(fees)) {
            List<FundActivity> activities = new ArrayList<>();
            TransactionServiceHelper.wrapFeeActivitiesForAccount(activities, fees);
            fundStreamEngine.submit(fundTransaction.getTargetId(), activities.toArray(new FundActivity[0]));
        }

        TransactionId transactionId = new TransactionId();
        transactionId.setId(fundTransaction.getId());
        transactionId.setSerialNo(fundTransaction.getSerialNo());
        return transactionId;
    }

    private void checkFeeTransaction(Transaction transaction)
    {
        // Even though use cash pipeline for fee, still need specify the account id(who pay the fee)
        AssertUtils.notNull(transaction.getAccountId(), "Argument missed: accountId");
        AssertUtils.notEmpty(transaction.getFees(), "Argument missed: fees");
        AssertUtils.isTrue(transaction.getPipeline() == Pipeline.ACCOUNT ||
            transaction.getPipeline() == Pipeline.CASH, "Invalid transaction pipeline");
        if (transaction.getPipeline() == Pipeline.ACCOUNT) {
            AssertUtils.notNull(transaction.getPassword(), "Argument missed: password");
        }

        long totalFee = 0;
        // 所有费用必须使用一种渠道, 并且与请求的渠道一致
        for (Fee fee : transaction.getFees()) {
            totalFee += fee.getAmount();
            AssertUtils.isTrue(fee.getPipeline() == transaction.getPipeline(),
                "Fee pipeline != transaction pipeline");
            AssertUtils.notNull(fee.getType(), "Argument missed: fee type");
            AssertUtils.isTrue(fee.getType().isFeeType(), "Invalid fee type");
            AssertUtils.notNull(fee.getAmount(), "Argument missed: fee amount");
            AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
        }

        // 请求中无费用总额时设置计算总额totalFee，否则请求中的费用总和必须与计算总额totalFee一致
        if (transaction.getAmount() != null) {
            AssertUtils.isTrue(transaction.getAmount() == totalFee,
                "Transaction amount != Total fee amount");
        } else {
            transaction.setAmount(totalFee);
        }

    }
}
