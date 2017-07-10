package org.openjava.upay.core.dao;

import org.apache.ibatis.annotations.Param;
import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.core.type.AccountStatus;
import org.openjava.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository("fundAccountDao")
public interface IFundAccountDao extends MybatisMapperSupport
{
    void createFundAccount(FundAccount account);

    FundAccount findFundAccountById(Long accountId);

    int updateAccountLockStatus(@Param("id") Long id, @Param("lockStatus")AccountStatus lockStatus,
                                @Param("when") Date when);

    int compareAndSetStatus(@Param("id") Long id, @Param("newStatus")AccountStatus newStatus,
                            @Param("oldStatus")AccountStatus oldStatus, @Param("when") Date when);
}
