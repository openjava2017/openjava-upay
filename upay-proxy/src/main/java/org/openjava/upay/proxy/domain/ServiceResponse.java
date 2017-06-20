package org.openjava.upay.proxy.domain;

public class ServiceResponse<E>
{
    private int code;
    private String message;
    private E data;

    public ServiceResponse(int code, E data, String message)
    {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public int getCode()
    {
        return code;
    }

    public ServiceResponse setCode(int code)
    {
        this.code = code;
        return this;
    }

    public String getMessage()
    {
        return message;
    }

    public ServiceResponse setMessage(String message)
    {
        this.message = message;
        return this;
    }

    public E getData()
    {
        return data;
    }

    public ServiceResponse<E> setData(E data)
    {
        this.data = data;
        return this;
    }

    public static ServiceResponse<?> create(int code, String message)
    {
        return new ServiceResponse<>(code, null, message);
    }

    public static <E> ServiceResponse<E> create(int code, E data)
    {
        return new ServiceResponse<>(code, data, null);
    }

    public static <E> ServiceResponse<E> success(E data)
    {
        return create(0, data);
    }

    public static ServiceResponse success(String msg)
    {
        return create(0, msg);
    }

    public static ServiceResponse failure(int code, String msg)
    {
        return create(code, msg);
    }
}
