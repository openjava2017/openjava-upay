package org.openjava.upay.trade.service;

import org.openjava.upay.trade.support.ICallableServiceEndpoint;

public interface IServiceEndpointFactory
{
    void registerCallableServiceEndpoints(Object object, String componentId, String... methods);

    ICallableServiceEndpoint<?> getServiceEndpoint(String service);
}
