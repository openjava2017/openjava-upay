package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.trade.domain.FrozenTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.domain.UnfrozenTransaction;

public interface IFundTransactionService
{
    TransactionId freezeAccountFund(Merchant merchant, FrozenTransaction transaction);

    void unfreezeAccountFund(Merchant merchant, UnfrozenTransaction transaction);
}