package org.openjava.upay.core.service.impl;

import org.openjava.upay.core.dao.IAccountFundDao;
import org.openjava.upay.core.domain.FundActivity;
import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.AccountFund;
import org.openjava.upay.core.model.FundStatement;
import org.openjava.upay.core.service.IAccountFundService;
import org.openjava.upay.core.service.IFundStreamEngine;
import org.openjava.upay.core.type.Action;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.util.AssertUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 资金并发更新-乐观锁实现
 */
@Service("fundStreamEngine")
public class FundStreamEngineImpl implements IFundStreamEngine
{
    private static final int RETRIES = 3;

    @Resource
    private IAccountFundDao accountFundDao;

    @Resource
    private IAccountFundService accountFundService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void submit(Long accountId, FundActivity... activities)
    {
        AssertUtils.notNull(accountId);
        AssertUtils.notEmpty(activities);

        boolean success = true;
        for (int retry = 0; retry < RETRIES; retry ++) {
            AccountFund accountFund = accountFundService.findAccountFundById(accountId);
            if (accountFund == null) {
                throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
            }

            long totalAmount = 0;
            FundStatement[] statements = wrapFundStatements(accountFund, activities);
            for (FundStatement statement : statements) {
                totalAmount += statement.getAmount();
                accountFundDao.createFundStatement(statement);
            }

            // 设置账户余额，判断余额是否充足
            long balance = accountFund.getBalance() + totalAmount;
            if (balance >= 0) {
                accountFund.setBalance(balance);
            } else {
                throw new FundTransactionException(ErrorCode.INSUFFICIENT_ACCOUNT_FUNDS);
            }

            success = compareAndSetVersion(accountFund);
            if (success) {
                break;
            }
        }

        if (!success) {
            throw new FundTransactionException(ErrorCode.DATA_CONCURRENT_MODIFY);
        }
    }

    private FundStatement[] wrapFundStatements(AccountFund accountFund, FundActivity[] activities)
    {
        Date when = new Date();
        accountFund.setModifiedTime(when);
        Long balance = accountFund.getBalance();
        FundStatement[] statements = new FundStatement[activities.length];

        for (int i = 0; i < activities.length; i++) {
            FundStatement statement = new FundStatement();
            FundActivity activity = activities[i];
            long amount = Math.abs(activity.getAmount());
            statement.setAccountId(accountFund.getId());
            statement.setTransactionId(activity.getTransactionId());
            statement.setPipeline(activity.getPipeline());
            statement.setAction(activity.getAction());
            statement.setBalance(balance);
            if (activity.getAction() == Action.INCOME) {
                statement.setAmount(amount);
                balance += amount;
            } else if (activity.getAction() == Action.OUTGO){
                statement.setAmount(0 - amount);
                balance -= amount;
            }
            statement.setDescription(activity.getDescription());
            statement.setCreatedTime(when);

            statements[i] = statement;
        }

        return statements;
    }

    private boolean compareAndSetVersion(AccountFund accountFund)
    {
        return accountFundDao.compareAndSetVersion(accountFund) > 0;
    }
}
