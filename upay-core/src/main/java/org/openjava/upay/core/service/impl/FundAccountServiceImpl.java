package org.openjava.upay.core.service.impl;

import org.openjava.upay.core.dao.IFundAccountDao;
import org.openjava.upay.core.service.IFundAccountService;
import org.openjava.upay.core.type.AccountStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service("fundAccountService")
public class FundAccountServiceImpl implements IFundAccountService
{
    @Resource
    private IFundAccountDao fundAccountDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockFundAccount(Long accountId, Date when)
    {
        fundAccountDao.updateAccountLockStatus(accountId, AccountStatus.LOCKED, when);
    }
}