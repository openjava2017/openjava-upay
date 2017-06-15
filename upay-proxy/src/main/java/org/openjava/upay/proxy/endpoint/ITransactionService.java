package org.openjava.upay.proxy.endpoint;

public interface ITransactionService
{
    void commit(ServiceRequest<Data> request, ServiceResponse<Data> response);
}