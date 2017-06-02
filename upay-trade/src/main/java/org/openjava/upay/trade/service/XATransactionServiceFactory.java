package org.openjava.upay.trade.service;

public interface XATransactionServiceFactory
{
    void registerTransactionService(XATransactionService service);

    XATransactionService getTransactionService(String serviceId);
}
