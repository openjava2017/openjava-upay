package org.openjava.upay.core.service.impl;

import org.openjava.upay.core.dao.IAccountFundDao;
import org.openjava.upay.core.model.AccountFund;
import org.openjava.upay.core.service.IAccountFundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service("accountFundService")
public class AccountFundServiceImpl implements IAccountFundService
{
    @Resource
    private IAccountFundDao accountFundDao;

    /**
     * 新启数据查询事务用于乐观锁实现, 请勿修改事务传播属性
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public AccountFund findAccountFundById(Long accountId)
    {
        return accountFundDao.findAccountFundById(accountId);
    }
}
