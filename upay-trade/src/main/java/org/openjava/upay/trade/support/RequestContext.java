package org.openjava.upay.trade.support;

import org.openjava.upay.core.model.Merchant;

public class RequestContext
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
