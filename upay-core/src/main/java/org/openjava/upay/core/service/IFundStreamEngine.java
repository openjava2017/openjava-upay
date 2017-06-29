package org.openjava.upay.core.service;

import org.openjava.upay.core.domain.FundActivity;
import org.openjava.upay.core.model.AccountFund;

/**
 * 账户资金并发操作的乐观锁实现
 */
public interface IFundStreamEngine
{
    AccountFund submit(Long accountId, FundActivity... activities);

    AccountFund submitOnce(Long accountId, FundActivity... activities);

    boolean freeze(Long accountId, Long amount);

    boolean unfreeze(Long accountId, Long amount);
}
