package org.openjava.upay.trade.service;

import org.openjava.upay.trade.support.AbstractServiceComponent;
import org.openjava.upay.trade.support.ICallableEndpoint;

public interface IServiceEndpointFactory
{
    void registerServiceComponent(AbstractServiceComponent component);

    ICallableEndpoint<?> getServiceEndpoint(String service);
}
