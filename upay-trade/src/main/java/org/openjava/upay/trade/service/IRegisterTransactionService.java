package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.trade.domain.AccountId;
import org.openjava.upay.trade.domain.RegisterTransaction;

public interface IRegisterTransactionService
{
    AccountId register(Merchant merchant, RegisterTransaction transaction) throws Exception;
}
