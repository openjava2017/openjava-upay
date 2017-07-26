package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.trade.domain.RefundTransaction;
import org.openjava.upay.trade.domain.TransactionId;

public interface IRefundTransactionService
{
    TransactionId refund(Merchant merchant, RefundTransaction transaction) throws Exception;
}
