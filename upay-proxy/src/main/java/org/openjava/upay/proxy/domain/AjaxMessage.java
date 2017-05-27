package org.openjava.upay.proxy.domain;

public class AjaxMessage<E>
{
    private int code;
    private String message;
    private E data;

    public AjaxMessage(int code, E data, String message)
    {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public int getCode()
    {
        return code;
    }

    public AjaxMessage setCode(int code)
    {
        this.code = code;
        return this;
    }

    public String getMessage()
    {
        return message;
    }

    public AjaxMessage setMessage(String message)
    {
        this.message = message;
        return this;
    }

    public E getData()
    {
        return data;
    }

    public AjaxMessage<E> setData(E data)
    {
        this.data = data;
        return this;
    }

    public static AjaxMessage<?> create(int code, String message)
    {
        return new AjaxMessage<>(code, null, message);
    }

    public static <E> AjaxMessage<E> create(int code, E data)
    {
        return new AjaxMessage<>(code, data, null);
    }

    public static <E> AjaxMessage<E> success(E data)
    {
        return create(0, data);
    }

    public static AjaxMessage success(String msg)
    {
        return create(0, msg);
    }

    public static AjaxMessage failure(int code, String msg)
    {
        return create(code, msg);
    }
}
