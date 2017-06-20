package org.openjava.upay.proxy.domain;

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

    public String endpointId()
    {
        return method.getName();
    }

    public Class<T> getRequiredType()
    {
        return requiredType;
    }

    public Object call(ServiceRequest<T> request) throws Exception
    {
        return method.invoke(target, request);
    }

    public static <E> CallableServiceEndpoint<E> create(Object target, Method method, Class<E> requiredType)
    {
        return new CallableServiceEndpoint<>(target, method, requiredType);
    }
}
