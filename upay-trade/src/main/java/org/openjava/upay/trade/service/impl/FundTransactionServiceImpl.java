package org.openjava.upay.trade.service.impl;

import org.openjava.upay.core.dao.IFundAccountDao;
import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.core.type.AccountStatus;
import org.openjava.upay.shared.redis.IRedisSystemService;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.service.IFundTransactionService;
import org.openjava.upay.util.DateUtils;
import org.openjava.upay.util.ObjectUtils;
import org.openjava.upay.util.security.PasswordUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service("fundTransactionService")
public class FundTransactionServiceImpl implements IFundTransactionService
{
    private static final String TRADE_AUTH_PREFIX = "upay:trade:auth:";

    private static final int MAX_PASSWORD_ERRORS = 3;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IRedisSystemService redisSystemService;

    public void checkPaymentPermission(FundAccount account, String password) throws Exception
    {
        Date when = new Date();
        String date = DateUtils.format(when, DateUtils.YYYY_MM_DD);
        String errorsDailyKey = TRADE_AUTH_PREFIX + date + "[" + account.getId() + "]";
        String encodedPwd = PasswordUtils.encrypt(password, account.getSecretKey());
        if (!ObjectUtils.equals(account.getTradePwd(), encodedPwd)) {
            long expiredInSec = TimeUnit.DAYS.toSeconds(2);
            long errors = redisSystemService.incAndGet(errorsDailyKey, (int) expiredInSec);
            if (errors >= MAX_PASSWORD_ERRORS) {
                fundAccountDao.updateAccountLockStatus(account.getId(), AccountStatus.LOCKED, when);
            }

            throw MAX_PASSWORD_ERRORS - errors == 1 ?
                    new FundTransactionException("账户密码错误, 再错误一次将锁定账户", ErrorCode.INVALID_ACCOUNT_PASSWORD.getCode()) :
                    new FundTransactionException(ErrorCode.INVALID_ACCOUNT_PASSWORD);
        }
        redisSystemService.remove(errorsDailyKey);
    }
}
