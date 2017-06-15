package org.openjava.upay.proxy.endpoint;

public class ServiceResponse<T>
{
    private T data;

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }
}
