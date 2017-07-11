package org.openjava.upay.trade.dao;

import org.openjava.upay.shared.mybatis.MybatisMapperSupport;
import org.openjava.upay.trade.domain.UnfrozenRequest;
import org.openjava.upay.trade.model.FundFrozen;
import org.springframework.stereotype.Component;

@Component("fundFrozenDao")
public interface IFundFrozenDao extends MybatisMapperSupport
{
    void freezeAccountFund(FundFrozen fundFrozen);

    FundFrozen findFundFrozenByNo(String serialNo);

    FundFrozen findFundFrozenById(Long id);

    int unfreezeAccountFund(UnfrozenRequest request);
}
