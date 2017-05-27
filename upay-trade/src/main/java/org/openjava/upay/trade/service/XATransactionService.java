package org.openjava.upay.trade.service;

import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.type.TransactionType;

public interface XATransactionService
{
    Transaction beginTransaction(Transaction transaction) throws Exception;

    Transaction commitTransaction(Transaction transaction) throws Exception;

    Transaction rollBackTransaction(Transaction transaction) throws Exception;

    TransactionType getTransactionType();
}
