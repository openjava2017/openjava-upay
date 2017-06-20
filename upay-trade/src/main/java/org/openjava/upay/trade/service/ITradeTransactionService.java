package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.trade.domain.TradeTransaction;
import org.openjava.upay.trade.domain.TransactionId;

public interface ITradeTransactionService
{
    TransactionId trade(Merchant merchant, TradeTransaction transaction) throws Exception;
}