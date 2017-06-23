package org.openjava.upay.core.service;

import java.util.Date;

public interface IFundAccountService
{
    void lockFundAccount(Long accountId, Date when);
}
