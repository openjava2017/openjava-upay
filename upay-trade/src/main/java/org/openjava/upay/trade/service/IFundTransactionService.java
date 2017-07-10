package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.FundAccount;
import org.openjava.upay.trade.domain.FrozenTransaction;
import org.openjava.upay.trade.domain.TransactionId;

public interface IFundTransactionService
{
    TransactionId freezeAccountFund(FrozenTransaction transaction);

    void checkPaymentPermission(FundAccount account, String password) throws Exception;
}
