package org.openjava.upay.trade.service;

import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.support.ServiceRequest;

public interface IFeeTransactionService
{
    TransactionId submit(ServiceRequest<Transaction> request) throws Exception;
}
