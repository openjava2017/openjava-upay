package org.openjava.upay.trade.support;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractServiceComponent
{
    public abstract String componentId();

    public ICallableEndpoint[] scanEndpoints()
    {
        // Only scan public methods
        Method[] methods = this.getClass().getMethods();
        List<ICallableEndpoint> endpoints = new ArrayList<>();

        for (Method method : methods) {
            Type[] types = method.getGenericParameterTypes();
            // Only one parameter ServiceRequest<T> in method
            if (types.length == 1 && types[0] instanceof ParameterizedType) {
                ParameterizedType type = (ParameterizedType) types[0];
                if(type.getRawType() == ServiceRequest.class) {
                    // Get class type T in ServiceRequest<T>
                    Class<?> requiredType = (Class) type.getActualTypeArguments()[0];
                    ICallableEndpoint endpoint = new CallableServiceEndpoint<>(method, requiredType);
                    endpoints.add(endpoint);
                }
            }
        }
        return endpoints.toArray(new ICallableEndpoint[0]);
    }

    private class CallableServiceEndpoint<T> implements ICallableEndpoint<T>
    {
        private Method method;
        // that is T class type in ServiceRequest<T>
        private Class<T> requiredType;

        private CallableServiceEndpoint(Method method, Class<T> requiredType)
        {
            this.method = method;
            this.requiredType = requiredType;
        }

        @Override
        public String endpointId()
        {
            return method.getName();
        }

        @Override
        public Class<T> getRequiredType()
        {
            return requiredType;
        }

        @Override
        public Object call(ServiceRequest<T> request) throws Exception
        {
            return method.invoke(AbstractServiceComponent.this, request);
        }
    }
}
