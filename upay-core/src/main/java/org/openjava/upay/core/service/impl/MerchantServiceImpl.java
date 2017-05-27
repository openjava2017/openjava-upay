package org.openjava.upay.core.service.impl;

import org.openjava.upay.core.dao.IMerchantDao;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IMerchantService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("merchantService")
public class MerchantServiceImpl implements IMerchantService
{
    @Resource
    private IMerchantDao merchantDao;

    @Override
    public Merchant findMerchantById(Long id)
    {
        return merchantDao.findMerchantById(id);
    }
}
