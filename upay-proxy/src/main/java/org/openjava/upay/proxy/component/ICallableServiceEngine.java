package org.openjava.upay.proxy.component;

import org.openjava.upay.proxy.domain.CallableServiceEndpoint;

public interface ICallableServiceEngine
{
    void registerCallableServiceEndpoints(Object object, String componentId, String... methods);

    CallableServiceEndpoint<?> getCallableServiceEndpoint(String service);
}
