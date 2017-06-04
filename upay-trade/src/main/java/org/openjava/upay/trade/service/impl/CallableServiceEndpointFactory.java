package org.openjava.upay.trade.service.impl;

import org.openjava.upay.Constants;
import org.openjava.upay.trade.service.IServiceEndpointFactory;
import org.openjava.upay.trade.support.AbstractServiceComponent;
import org.openjava.upay.trade.support.ICallableEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("transactionServiceFactory")
public class CallableServiceEndpointFactory implements IServiceEndpointFactory, BeanPostProcessor
{
    private static Logger LOG = LoggerFactory.getLogger(CallableServiceEndpointFactory.class);

    private Map<String, ICallableEndpoint<?>> endpoints = new ConcurrentHashMap();

    @Override
    public void registerServiceComponent(AbstractServiceComponent component)
    {
        ICallableEndpoint[] endpoints = component.scanEndpoints();
        for (ICallableEndpoint endpoint : endpoints) {
            String endpointId = endpoint.endpointId();
            // Register default service endpoint
            if (Constants.DEFAULT_ENDPOINT_ID.equals(endpointId)) {
                this.endpoints.put(component.componentId(), endpoint);
            }
            this.endpoints.put(component.componentId() + Constants.COLON + endpointId, endpoint);
            LOG.debug("Service endpoint {}:{} registered", component.componentId(), endpointId);
        }
    }

    @Override
    public ICallableEndpoint<?> getServiceEndpoint(String service)
    {
        return endpoints.get(service);
    }

    @Override
    public Object postProcessBeforeInitialization(Object object, String name) throws BeansException
    {
        return object;
    }

    @Override
    public Object postProcessAfterInitialization(Object object, String name) throws BeansException
    {
        if (object instanceof AbstractServiceComponent) {
            AbstractServiceComponent component = (AbstractServiceComponent) object;
            registerServiceComponent(component);
            LOG.info("Service component {} registered", component.componentId());
        }
        return object;
    }
}
