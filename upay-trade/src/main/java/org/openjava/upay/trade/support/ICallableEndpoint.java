package org.openjava.upay.trade.support;

public interface ICallableEndpoint<T>
{
    String endpointId();

    Class<T> getRequiredType();

    Object call(ServiceRequest<T> request) throws Exception;
}