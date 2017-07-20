package org.openjava.upay.trade.service;

import org.openjava.upay.core.model.FundAccount;

public interface IPasswordService
{
    void checkPaymentPermission(FundAccount account, String password) throws Exception;
}
