package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.trade.domain.AccountId;
import org.openjava.upay.trade.domain.RegisterTransaction;
import org.openjava.upay.trade.domain.TransactionId;

public interface IAccountTransactionService
{
    AccountId register(Merchant merchant, RegisterTransaction transaction) throws Exception;

    void freezeFundAccount(Long accountId);

    void unfreezeFundAccount(Long accountId);
}
