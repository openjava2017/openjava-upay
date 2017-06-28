package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.dao.IAccountFundDao;
import org.openjava.upay.core.dao.IFundAccountDao;
import org.openjava.upay.core.model.AccountFund;
import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.type.AccountStatus;
import org.openjava.upay.shared.sequence.IKeyGenerator;
import org.openjava.upay.shared.sequence.KeyGeneratorManager;
import org.openjava.upay.trade.domain.AccountId;
import org.openjava.upay.trade.domain.RegisterTransaction;
import org.openjava.upay.trade.service.IRegisterTransactionService;
import org.openjava.upay.util.AssertUtils;
import org.openjava.upay.util.security.AESCipher;
import org.openjava.upay.util.security.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service("registerTransactionService")
public class RegisterTransactionServiceImpl implements IRegisterTransactionService
{
    private static final Logger LOG = LoggerFactory.getLogger(RegisterTransactionServiceImpl.class);

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IAccountFundDao accountFundDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountId register(Merchant merchant, RegisterTransaction transaction) throws Exception
    {
        // Arguments check
        Date when = new Date();
        checkRegisterTransaction(transaction);

        LOG.info("Handle fund account register request");
        FundAccount account = wrapFundAccount(merchant, transaction, when);
        fundAccountDao.createFundAccount(account);
        AccountFund fund = wrapAccountFund(account, when);
        accountFundDao.createAccountFund(fund);

        AccountId registerId = new AccountId();
        registerId.setId(account.getId());
        return registerId;
    }

    private void checkRegisterTransaction(RegisterTransaction transaction)
    {
        AssertUtils.notNull(transaction.getType(), "Argument missed: type");
        AssertUtils.notNull(transaction.getName(), "Argument missed: name");
        AssertUtils.notNull(transaction.getMobile(), "Argument missed: mobile");
        AssertUtils.notNull(transaction.getPassword(), "Argument missed: password");
    }

    private FundAccount wrapFundAccount(Merchant merchant, RegisterTransaction transaction, Date when) throws Exception
    {
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(KeyGeneratorManager.SequenceKey.FUND_ACCOUNT);
        String secretKey = AESCipher.generateSecretKey();
        String encodedPwd = PasswordUtils.encrypt(transaction.getPassword(), secretKey);
        long accountId = transaction.getId() == null ? keyGenerator.nextId() : transaction.getId();
        FundAccount account = new FundAccount();
        account.setId(accountId);
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
}
