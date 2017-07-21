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
    public AccountFund submit(Long accountId, FundActivity... activities)
    {
        AssertUtils.notNull(accountId);
        AssertUtils.notEmpty(activities);

        boolean success = true;
        AccountFund accountFund = null;
        FundStatement[] statements = null;
        for (int retry = 0; retry < RETRIES; retry ++) {
            accountFund = accountFundService.findAccountFundById(accountId);
            if (accountFund == null) {
                throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
            }

            long totalAmount = 0;
            statements = wrapFundStatements(accountFund, activities);
            for (FundStatement statement : statements) {
                totalAmount += statement.getAmount();
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
            throw new FundTransactionException(ErrorCode.FUND_TRANSACTION_FAILED);
        }

        // 创建资金流水
        for (FundStatement statement : statements) {
            accountFundDao.createFundStatement(statement);
        }

        return accountFund;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public AccountFund submitOnce(Long accountId, FundActivity... activities)
    {
        AssertUtils.notNull(accountId);
        AssertUtils.notEmpty(activities);

        // NOTE: mysql database's default isolation is REPEATABLE_READ, that means the same AccountFund
        // will be returned when this method is repeated invoked within a specified database transaction
        // If we have retry mechanism, please use submit() instead
        AccountFund accountFund = accountFundDao.findAccountFundById(accountId);
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

        boolean success = compareAndSetVersion(accountFund);
        if (!success) {
            throw new FundTransactionException(ErrorCode.FUND_TRANSACTION_FAILED);
        }

        return accountFund;
    }

    @Override
    public boolean freeze(Long accountId, Long amount)
    {
        AssertUtils.notNull(accountId);
        AssertUtils.notNull(amount);
        AssertUtils.isTrue(amount > 0, "Invalid freeze amount");

        boolean success = true;
        for (int retry = 0; retry < RETRIES; retry ++) {
            AccountFund accountFund = accountFundService.findAccountFundById(accountId);
            if (accountFund == null) {
                throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
            }

            // 设置账户余额，判断余额是否充足
            long balance = accountFund.getBalance() - amount;
            long frozenAmount = accountFund.getFrozenAmount() + amount;
            if (balance >= 0) {
                accountFund.setBalance(balance);
                accountFund.setFrozenAmount(frozenAmount);
            } else {
                throw new FundTransactionException(ErrorCode.INSUFFICIENT_ACCOUNT_FUNDS);
            }

            success = compareAndSetVersion(accountFund);
            if (success) {
                break;
            }
        }

        return success;
    }

    @Override
    public boolean unfreeze(Long accountId, Long amount)
    {
        AssertUtils.notNull(accountId);
        AssertUtils.notNull(amount);
        AssertUtils.isTrue(amount > 0, "Invalid freeze amount");

        boolean success = true;
        for (int retry = 0; retry < RETRIES; retry ++) {
            AccountFund accountFund = accountFundService.findAccountFundById(accountId);
            if (accountFund == null) {
                throw new FundTransactionException(ErrorCode.ACCOUNT_NOT_FOUND);
            }

            // 设置账户余额，判断余额是否充足
            long balance = accountFund.getBalance() + amount;
            long frozenAmount = accountFund.getFrozenAmount() - amount;
            if (frozenAmount >= 0) {
                accountFund.setBalance(balance);
                accountFund.setFrozenAmount(frozenAmount);
            } else {
                throw new FundTransactionException(ErrorCode.INSUFFICIENT_ACCOUNT_FUNDS);
            }

            success = compareAndSetVersion(accountFund);
            if (success) {
                break;
            }
        }

        return success;
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
            statement.setType(activity.getType().getStatementType());
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
