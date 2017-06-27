package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.trade.domain.RegisterTransaction;
import org.openjava.upay.trade.domain.TransactionId;

public interface IRegisterTransactionService
{
    TransactionId register(Merchant merchant, RegisterTransaction transaction) throws Exception;
}
