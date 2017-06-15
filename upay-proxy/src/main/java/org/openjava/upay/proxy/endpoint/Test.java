package org.openjava.upay.proxy.endpoint;

import org.openjava.upay.util.json.JsonUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Test
{
    public static void main(String[] args) throws Exception
    {
        ITransactionService service = new TransactionServiceImpl();
//        ServiceRequest request = new ServiceRequest();
//        service.commit(request);

        Method method = service.getClass().getDeclaredMethod("commit", ServiceRequest.class, ServiceResponse.class);
        Type[] types = method.getGenericParameterTypes();
        ParameterizedType type = (ParameterizedType)types[0];
        Type[] types1 = type.getActualTypeArguments();
        Object data = JsonUtils.fromJsonString("{\"name\": \"huang\"}", (Class)types1[0]);
        ServiceRequest request = new ServiceRequest();
        request.setData(data);
        method.invoke(service, request, new ServiceResponse<>());
//        service.commit(request, new ServiceResponse());
//        System.out.println(((Data)data).getName());
//        for (Type type : types) {
//            if (type instanceof Class) { //是否为泛型
//                ParameterizedType type1 = (ParameterizedType)type;
//            }
//            System.out.println(type);
//        }

//        Type genType = service.getClass().getGenericSuperclass();
//        Class<?> c = (Class)genType;
//        genType = c.getGenericSuperclass();
//        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
//       // Type[] params1 = ((ParameterizedType) params[0]).getActualTypeArguments();
//        Class<?> entityClass = (Class) params[0];
//        System.out.println(entityClass);
//
//        request.setData(new Data());
//        ServiceResponse<Data> response = service.commit(request);
    }
}
