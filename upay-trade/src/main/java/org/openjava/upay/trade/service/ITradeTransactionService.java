package org.openjava.upay.trade.service;

import org.openjava.upay.trade.domain.TradeCommitTransaction;
import org.openjava.upay.trade.domain.TradePrepareTransaction;
import org.openjava.upay.trade.domain.TradeTransaction;
import org.openjava.upay.trade.domain.TransactionId;
import org.openjava.upay.trade.support.ServiceRequest;

public interface ITradeTransactionService
{
    TransactionId prepare(ServiceRequest<TradePrepareTransaction> request) throws Exception;

    TransactionId commit(ServiceRequest<TradeCommitTransaction> request) throws Exception;

    TransactionId submit(ServiceRequest<TradeTransaction> request) throws Exception;
}