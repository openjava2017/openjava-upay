package org.openjava.upay.core.service;

import org.openjava.upay.core.domain.FundActivity;

/**
 * 账户资金并发操作的乐观锁实现
 */
public interface IFundStreamEngine
{
    void submit(Long accountId, FundActivity... activities);
}
