package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.trade.domain.FlushTransaction;
import org.openjava.upay.trade.domain.TransactionId;

public interface IFlushTransactionService
{
    TransactionId flush(Merchant merchant, FlushTransaction transaction) throws Exception;
}
