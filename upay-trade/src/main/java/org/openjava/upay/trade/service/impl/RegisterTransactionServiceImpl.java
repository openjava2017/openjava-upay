package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.dao.IAccountFundDao;
import org.openjava.upay.core.dao.IFundAccountDao;
import org.openjava.upay.core.domain.FundActivity;
import org.openjava.upay.core.model.AccountFund;
import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IFundStreamEngine;
import org.openjava.upay.core.type.AccountStatus;
import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.shared.sequence.IKeyGenerator;
import org.openjava.upay.shared.sequence.ISerialKeyGenerator;
import org.openjava.upay.shared.sequence.KeyGeneratorManager;
import org.openjava.upay.trade.dao.IFundTransactionDao;
import org.openjava.upay.trade.domain.Fee;
import org.openjava.upay.trade.domain.RegisterTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.model.TransactionFee;
import org.openjava.upay.trade.service.IRegisterTransactionService;
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;
import org.openjava.upay.trade.util.TransactionServiceHelper;
import org.openjava.upay.util.AssertUtils;
import org.openjava.upay.util.ObjectUtils;
import org.openjava.upay.util.security.AESCipher;
import org.openjava.upay.util.security.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

public class RegisterTransactionServiceImpl implements IRegisterTransactionService
{
    private static final Logger LOG = LoggerFactory.getLogger(RegisterTransactionServiceImpl.class);

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IAccountFundDao accountFundDao;

    @Resource
    private IFundTransactionDao fundTransactionDao;

    @Resource
    private IFundStreamEngine fundStreamEngine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionId register(Merchant merchant, RegisterTransaction transaction) throws Exception
    {
        // Arguments check
        Date when = new Date();
        checkRegisterTransaction(transaction);

        LOG.info("Handle fund account register request");
        FundAccount account = wrapFundAccount(merchant, transaction, when);
        fundAccountDao.createFundAccount(account);
        AccountFund fund = wrapAccountFund(account, when);
        accountFundDao.createAccountFund(fund);

        // 处理缴费
        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            FundTransaction fundTransaction = wrapFeeTransaction(merchant, account, transaction.getFees(), when);
            fundTransactionDao.createFundTransaction(fundTransaction);

            List<TransactionFee> fees = TransactionServiceHelper.wrapTransactionFees(
                fundTransaction.getId(), transaction.getFees(), when);
            for (TransactionFee fee : fees) {
                fundTransactionDao.createTransactionFee(fee);
            }

            // 处理商户账户-费用收入, 只有现金渠道的缴费，只需处理商户账户
            List<FundActivity> activities = TransactionServiceHelper.wrapFeeActivitiesForMer(fees);
            fundStreamEngine.submit(merchant.getAccountId(), activities.toArray(new FundActivity[0]));
        }

        TransactionId transactionId = new TransactionId();
        transactionId.setId(account.getId());
        return transactionId;
    }

    private void checkRegisterTransaction(RegisterTransaction transaction)
    {
        AssertUtils.notNull(transaction.getType(), "Argument missed: type");
        AssertUtils.notNull(transaction.getName(), "Argument missed: name");
        AssertUtils.notNull(transaction.getMobile(), "Argument missed: mobile");
        AssertUtils.notNull(transaction.getPassword(), "Argument missed: password");

        // 注册账号费用必须使用现金渠道
        if (ObjectUtils.isNotEmpty(transaction.getFees())) {
            for (Fee fee : transaction.getFees()) {
                AssertUtils.isTrue(fee.getPipeline() == Pipeline.CASH, "Invalid fee pipeline");
                AssertUtils.isTrue(fee.getAmount() != null && fee.getAmount() > 0,"Invalid fee amount");
            }
        }
    }

    private FundAccount wrapFundAccount(Merchant merchant, RegisterTransaction transaction, Date when) throws Exception
    {
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(KeyGeneratorManager.SequenceKey.FUND_ACCOUNT);
        String secretKey = AESCipher.generateSecretKey();
        String encodedPwd = PasswordUtils.encrypt(transaction.getPassword(), secretKey);
        FundAccount account = new FundAccount();
        account.setId(keyGenerator.nextId());
        account.setType(transaction.getType());
        account.setCode(transaction.getCode());
        account.setName(transaction.getName());
        account.setGender(transaction.getGender());
        account.setMobile(transaction.getMobile());
        account.setEmail(transaction.getEmail());
        account.setIdCode(transaction.getIdCode());
        account.setAddress(transaction.getAddress());
        account.setLoginPwd(encodedPwd);
        account.setPassword(encodedPwd);
        account.setPwdChange(false);
        account.setSecretKey(secretKey);
        account.setMerchantId(merchant.getId());
        account.setStatus(AccountStatus.NORMAL);
        account.setCreatedTime(when);
        return account;
    }

    private AccountFund wrapAccountFund(FundAccount account, Date when)
    {
        AccountFund fund = new AccountFund();
        fund.setId(account.getId());
        fund.setBalance(0L);
        fund.setFrozenAmount(0L);
        fund.setVersion(0);
        fund.setCreatedTime(when);
        return fund;
    }

    private FundTransaction wrapFeeTransaction(Merchant merchant, FundAccount account,
                                               List<Fee> fees, Date when)
    {
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(KeyGeneratorManager.SequenceKey.FUND_TRANSACTION);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        String serialNo = serialKeyGenerator.nextSerialNo(String.valueOf(TransactionType.DEPOSIT.getCode()),
                TransactionType.class.getSimpleName());

        long totalFee = 0;
        for (Fee fee : fees) {
            totalFee += fee.getAmount();
        }

        FundTransaction fundTransaction = new FundTransaction();
        fundTransaction.setId(keyGenerator.nextId());
        fundTransaction.setMerchantId(merchant.getId());
        fundTransaction.setSerialNo(serialNo);
        fundTransaction.setType(TransactionType.PAY_FEE);
        fundTransaction.setFromId(account.getId());
        fundTransaction.setFromName(account.getName());
        fundTransaction.setToId(merchant.getAccountId());
        fundTransaction.setToName(merchant.getName());
        fundTransaction.setPipeline(fees.get(0).getPipeline());
        fundTransaction.setAmount(totalFee);
        fundTransaction.setStatus(TransactionStatus.STATUS_COMPLETED);
        fundTransaction.setDescription(null);
        fundTransaction.setCreatedTime(when);
        fundTransaction.setModifiedTime(null);
        return fundTransaction;
    }
}
