package org.openjava.upay.core.service;

import org.openjava.upay.core.model.Merchant;

public interface IMerchantService
{
    Merchant findMerchantById(Long id);
}
