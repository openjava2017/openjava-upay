package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.trade.domain.FrozenFundTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.domain.UnfrozenFundTransaction;

public interface IFundTransactionService
{
    TransactionId freezeAccountFund(Merchant merchant, FrozenFundTransaction transaction);

    void unfreezeAccountFund(Merchant merchant, UnfrozenFundTransaction transaction);
}