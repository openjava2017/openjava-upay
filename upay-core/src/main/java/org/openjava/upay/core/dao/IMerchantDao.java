package org.openjava.upay.core.dao;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

@Repository("merchantDao")
public interface IMerchantDao extends MybatisMapperSupport
{
    Merchant findMerchantById(Long id);
}
