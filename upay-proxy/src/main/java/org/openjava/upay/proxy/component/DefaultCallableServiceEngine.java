package org.openjava.upay.proxy.component;

import org.openjava.upay.Constants;
import org.openjava.upay.proxy.domain.CallableServiceEndpoint;
import org.openjava.upay.proxy.domain.ServiceRequest;
import org.openjava.upay.proxy.util.CallableComponent;
import org.openjava.upay.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("callableServiceEndpointFactory")
public class DefaultCallableServiceEngine implements ICallableServiceEngine, BeanPostProcessor
{
    private static Logger LOG = LoggerFactory.getLogger(DefaultCallableServiceEngine.class);

    private Map<String, CallableServiceEndpoint<?>> endpoints = new ConcurrentHashMap();

    @Override
    public void registerCallableServiceEndpoints(Object object, String componentId, String... methods)
    {
        Class<?> classType = object.getClass();
        if (methods != null && methods.length > 0) {
            for (String name : methods) {
                try {
                    Method method = classType.getMethod(name, ServiceRequest.class);
                    registerCallableServiceEndpoint(object, componentId, method);
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
                    }
                }
            }
        }
    }

    @Override
    public CallableServiceEndpoint<?> getCallableServiceEndpoint(String service)
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
        // Do not scan AOP proxy object, since we cannot get required type(T in ServiceRequest<T>) from proxy object
        if (AopUtils.isAopProxy(object)) {
//            Class<?> rawType = AopUtils.getTargetClass(object);
            return object;
        }

        CallableComponent annotation = object.getClass().getAnnotation(CallableComponent.class);
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

            CallableServiceEndpoint endpoint = CallableServiceEndpoint.create(target, method, requiredType);
            if (ObjectUtils.equals(method.getName(), Constants.DEFAULT_ENDPOINT_ID)) {
                this.endpoints.put(componentId, endpoint);
            }
            this.endpoints.put(componentId + Constants.COLON + method.getName(), endpoint);
            LOG.info("Callable service endpoint ({}:{}) registered", componentId, method.getName());
        }
    }
}
