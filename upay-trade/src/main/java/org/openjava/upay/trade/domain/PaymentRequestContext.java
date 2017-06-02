package org.openjava.upay.trade.domain;

import org.openjava.upay.core.model.Merchant;

public class PaymentRequestContext
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
