package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.dao.IFundAccountDao;
import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IFundAccountService;
import org.openjava.upay.core.service.IFundStreamEngine;
import org.openjava.upay.core.type.AccountStatus;
import org.openjava.upay.shared.redis.IRedisSystemService;
import org.openjava.upay.shared.sequence.IKeyGenerator;
import org.openjava.upay.shared.sequence.ISerialKeyGenerator;
import org.openjava.upay.shared.sequence.KeyGeneratorManager;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.dao.IFundFrozenDao;
import org.openjava.upay.trade.domain.FrozenTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.domain.UnfrozenRequest;
import org.openjava.upay.trade.domain.UnfrozenTransaction;
import org.openjava.upay.trade.model.FundFrozen;
import org.openjava.upay.trade.service.IFundTransactionService;
import org.openjava.upay.trade.type.FrozenStatus;
import org.openjava.upay.trade.type.FrozenType;
import org.openjava.upay.util.AssertUtils;
import org.openjava.upay.util.DateUtils;
import org.openjava.upay.util.ObjectUtils;
import org.openjava.upay.util.security.PasswordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service("fundTransactionService")
public class FundTransactionServiceImpl implements IFundTransactionService
{
    private static final String TRADE_AUTH_PREFIX = "upay:register:auth:";

    private static final int MAX_PASSWORD_ERRORS = 3;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IFundFrozenDao fundFrozenDao;

    @Resource
    private IFundStreamEngine fundStreamEngine;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IRedisSystemService redisSystemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransactionId freezeAccountFund(Merchant merchant, FrozenTransaction transaction)
    {
        AssertUtils.notNull(transaction.getAccountId(), "Argument missed: accountId");
        AssertUtils.isTrue(transaction.getAmount() != null && transaction.getAmount() > 0,
                "Invalid freeze amount");

        Date when = new Date();
        FundAccount account = fundAccountDao.findFundAccountById(transaction.getAccountId());
        if (account == null || account.getStatus() == AccountStatus.LOGOFF) {
            throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (account.getStatus() != AccountStatus.NORMAL) {
            throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_STATUS);
        }

        boolean result = fundStreamEngine.freeze(transaction.getAccountId(), transaction.getAmount());
        if (!result) {
            throw new FundTransactionException(ErrorCode.FUND_TRANSACTION_FAILED);
        }

        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(KeyGeneratorManager.SequenceKey.FUND_FROZEN);
        ISerialKeyGenerator serialKeyGenerator = keyGeneratorManager.getSerialKeyGenerator();
        String serialNo = transaction.getSerialNo();
        if (ObjectUtils.isEmpty(serialNo)) {
            serialNo = serialKeyGenerator.nextSerialNo("40");
        }

        FundFrozen fundFrozen = new FundFrozen();
        fundFrozen.setId(keyGenerator.nextId());
        fundFrozen.setSerialNo(serialNo);
        fundFrozen.setAccountId(account.getId());
        fundFrozen.setAccountName(account.getName());
        fundFrozen.setType(FrozenType.SYSTEM_FROZEN);
        fundFrozen.setAmount(transaction.getAmount());
        fundFrozen.setStatus(FrozenStatus.FROZEN);
        fundFrozen.setFrozenTime(when);
        fundFrozen.setMerchantId(merchant.getId());
        fundFrozen.setFrozenUid(transaction.getUserId());
        fundFrozen.setFrozenUname(transaction.getUserName());
        fundFrozen.setDescription(transaction.getDescription());
        fundFrozenDao.freezeAccountFund(fundFrozen);

        TransactionId transactionId = new TransactionId();
        transactionId.setId(fundFrozen.getId());
        transactionId.setSerialNo(serialNo);
        return transactionId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeAccountFund(Merchant merchant, UnfrozenTransaction transaction)
    {
        AssertUtils.notEmpty(transaction.getSerialNo(), "Argument missed: serialNo");

        Date when = new Date();
        FundFrozen fundFrozen = fundFrozenDao.findFundFrozenByNo(transaction.getSerialNo());
        if (fundFrozen == null) {
            throw new FundTransactionException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
        if (fundFrozen.getStatus() == FrozenStatus.UNFROZEN) {
            return;
        }
        fundStreamEngine.unfreeze(fundFrozen.getAccountId(), fundFrozen.getAmount());
        UnfrozenRequest request = new UnfrozenRequest();
        request.setId(fundFrozen.getId());
        request.setNewStatus(FrozenStatus.UNFROZEN);
        request.setOldStatus(FrozenStatus.FROZEN);
        request.setUnfrozenTime(when);
        request.setUnfrozenUid(transaction.getUserId());
        request.setUnfrozenUname(transaction.getUserName());
        fundFrozenDao.unfreezeAccountFund(request);
    }

    public void checkPaymentPermission(FundAccount account, String password) throws Exception
    {
        Date when = new Date();
        String date = DateUtils.format(when, DateUtils.YYYY_MM_DD);
        String errorsDailyKey = TRADE_AUTH_PREFIX + date + "[" + account.getId() + "]";
        String encodedPwd = PasswordUtils.encrypt(password, account.getSecretKey());
        if (!ObjectUtils.equals(account.getPassword(), encodedPwd)) {
            long expiredInSec = TimeUnit.DAYS.toSeconds(2);
            long errors = redisSystemService.incAndGet(errorsDailyKey, (int) expiredInSec);
            if (errors >= MAX_PASSWORD_ERRORS) {
                fundAccountService.lockFundAccount(account.getId(), when);
            }

            if (MAX_PASSWORD_ERRORS - errors > 1) {
                throw new FundTransactionException(ErrorCode.INVALID_ACCOUNT_PASSWORD);
            } else if (MAX_PASSWORD_ERRORS - errors == 1) {
                throw new FundTransactionException("账户密码错误, 再错误一次将锁定账户",
                    ErrorCode.INVALID_ACCOUNT_PASSWORD.getCode());
            } else {
                throw new FundTransactionException("账户密码错误, 已锁定账户",
                    ErrorCode.INVALID_ACCOUNT_PASSWORD.getCode());
            }
        }
        redisSystemService.remove(errorsDailyKey);
    }
}
