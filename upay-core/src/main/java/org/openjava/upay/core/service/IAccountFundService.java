package org.openjava.upay.core.service;

import org.openjava.upay.core.model.AccountFund;

public interface IAccountFundService
{
    AccountFund findAccountFundById(Long accountId);
}
