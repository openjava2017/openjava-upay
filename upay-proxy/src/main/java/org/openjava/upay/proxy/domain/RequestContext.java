package org.openjava.upay.proxy.domain;

import org.openjava.upay.core.model.Merchant;

import java.util.HashMap;

public class RequestContext extends HashMap<String, Object>
{
    private Merchant merchant;

    public Merchant getMerchant()
    {
        return merchant;
    }

    public void setMerchant(Merchant merchant)
    {
        this.merchant = merchant;
    }
}
