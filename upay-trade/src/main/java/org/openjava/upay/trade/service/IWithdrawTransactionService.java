package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.domain.TransactionId;

public interface IWithdrawTransactionService
{
    TransactionId withdraw(Merchant merchant, Transaction transaction) throws Exception;
}
