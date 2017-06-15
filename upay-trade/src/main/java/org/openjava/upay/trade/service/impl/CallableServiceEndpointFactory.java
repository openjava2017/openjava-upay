package org.openjava.upay.trade.service.impl;

import org.openjava.upay.Constants;
import org.openjava.upay.trade.service.IServiceEndpointFactory;
import org.openjava.upay.trade.support.CallableComponent;
import org.openjava.upay.trade.support.CallableServiceEndpoint;
import org.openjava.upay.trade.support.ICallableServiceEndpoint;
import org.openjava.upay.trade.support.ServiceRequest;
import org.openjava.upay.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("callableServiceEndpointFactory")
public class CallableServiceEndpointFactory implements IServiceEndpointFactory, BeanPostProcessor
{
    private static Logger LOG = LoggerFactory.getLogger(CallableServiceEndpointFactory.class);

    private Map<String, ICallableServiceEndpoint<?>> endpoints = new ConcurrentHashMap();

    @Override
    public void registerCallableServiceEndpoints(Object object, String componentId, String... methods)
    {
        Class<?> classType = object.getClass();
        if (methods != null) {
            for (String name : methods) {
                try {
                    Method method = classType.getMethod(name, ServiceRequest.class);
                    registerCallableServiceEndpoint(object, componentId, method);
                    LOG.info("Callable service endpoint {}:{} registered", componentId, name);
                } catch (NoSuchMethodException mex) {
                    LOG.warn("Callable service endpoint({}:{}) not found" + componentId, name);
                }
            }
        } else {
            // Only scan public methods
            for (Method method : classType.getMethods()) {
                Type[] types = method.getGenericParameterTypes();
                // Only one parameter ServiceRequest<T> in method
                if (types.length == 1 && types[0] instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) types[0];
                    if(type.getRawType() == ServiceRequest.class) {
                        registerCallableServiceEndpoint(object, componentId, method);
                        LOG.info("Callable service endpoint {}:{} registered", componentId, method.getName());
                    }
                }
            }
        }
    }

    @Override
    public ICallableServiceEndpoint<?> getServiceEndpoint(String service)
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
        Class<?> rawType = AopUtils.isAopProxy(object) ? AopUtils.getTargetClass(object) : object.getClass();

        CallableComponent annotation = rawType.getAnnotation(CallableComponent.class);
        if (annotation != null) {
            String componentId = annotation.id();
            if (componentId == null) {
                componentId = name;
                LOG.warn("Service component id not set, use bean name instead: {}", componentId);
            }
            try {
                registerCallableServiceEndpoints(object, componentId, annotation.methods());
            } catch (Exception ex) {
                LOG.error("Register callable service endpoint(" + componentId + ") failed", ex);
            }
        }

        return object;
    }

    protected void registerCallableServiceEndpoint(Object target, String componentId, Method method)
    {
        Type[] types = method.getGenericParameterTypes();
        // Only one parameter ServiceRequest<T> in method
        if (types.length == 1 && types[0] instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) types[0];
            // Get class type T in ServiceRequest<T>
            Class<?> requiredType;
            Type dataType = type.getActualTypeArguments()[0];
            if (dataType instanceof ParameterizedType) {
                requiredType = (Class)((ParameterizedType) dataType).getRawType();
            } else {
                requiredType = (Class) dataType;
            }

            ICallableServiceEndpoint endpoint = CallableServiceEndpoint.create(target, method, requiredType);
            if (ObjectUtils.equals(method.getName(), Constants.DEFAULT_ENDPOINT_ID)) {
                this.endpoints.put(componentId, endpoint);
            }
            this.endpoints.put(componentId + Constants.COLON + method.getName(), endpoint);
        }
    }
}
