package org.openjava.upay.proxy.domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CallableServiceEndpoint<T>
{
    private Object target;
    private Method method;
    // that is T class type in ServiceRequest<T>
    private Class<T> requiredType;

    private CallableServiceEndpoint(Object target, Method method, Class<T> requiredType)
    {
        this.target = target;
        this.method = method;
        this.requiredType = requiredType;
    }

    public Class<T> getRequiredType()
    {
        return requiredType;
    }

    public Object call(ServiceRequest<T> request) throws Throwable
    {
        try {
            return method.invoke(target, request);
        } catch (InvocationTargetException tex) {
            throw tex.getCause() == null ? tex : tex.getCause();
        }
    }

    public static <E> CallableServiceEndpoint<E> create(Object target, Method method, Class<E> requiredType)
    {
        return new CallableServiceEndpoint<>(target, method, requiredType);
    }
}
