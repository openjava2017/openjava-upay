package org.openjava.upay.trade.dao;

import org.openjava.upay.shared.mybatis.MybatisMapperSupport;
import org.openjava.upay.trade.domain.UnfrozenFundRequest;
import org.openjava.upay.trade.model.FrozenTransaction;
import org.springframework.stereotype.Component;

@Component("frozenTransactionDao")
public interface IFrozenTransactionDao extends MybatisMapperSupport
{
    void freezeAccountFund(FrozenTransaction fundFrozen);

    FrozenTransaction findFundFrozenByNo(String serialNo);

    FrozenTransaction findFundFrozenById(Long id);

    int unfreezeAccountFund(UnfrozenFundRequest request);
}
