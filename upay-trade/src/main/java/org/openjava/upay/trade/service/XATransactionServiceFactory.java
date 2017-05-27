package org.openjava.upay.trade.service;

import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.type.Phase;

public interface XATransactionServiceFactory
{
    Transaction submit(Phase phase, Transaction transaction) throws Exception;

    void registerTransactionService(XATransactionService service);

    XATransactionService getTransactionService(Transaction transaction);
}
