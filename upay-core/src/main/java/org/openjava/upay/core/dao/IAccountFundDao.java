package org.openjava.upay.core.dao;

import org.openjava.upay.core.model.AccountFund;
import org.openjava.upay.core.model.FundStatement;
import org.openjava.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

@Repository("accountFundDao")
public interface IAccountFundDao extends MybatisMapperSupport
{
    void createAccountFund(AccountFund accountFund);

    AccountFund findAccountFundById(Long accountId);

    int compareAndSetVersion(AccountFund accountFund);

    void createFundStatement(FundStatement statement);
}