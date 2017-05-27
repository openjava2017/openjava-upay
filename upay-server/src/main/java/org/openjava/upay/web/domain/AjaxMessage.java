package org.openjava.upay.web.domain;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AjaxMessage<E>
{
    private String code;
    private String message;
    private E data;

    public AjaxMessage(String code, E data, String message)
    {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public String getCode()
    {
        return code;
    }

    public AjaxMessage setCode(String code)
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

    public static AjaxMessage<?> create(String code, String message)
    {
        return new AjaxMessage<>(code, null, message);
    }

    public static <E> AjaxMessage<E> create(String code, E data)
    {
        return new AjaxMessage<>(code, data, null);
    }

    public static AjaxMessage success(String msg)
    {
        return create("success", msg);
    }

    public static AjaxMessage success()
    {
        return success("操作成功");
    }

    public static AjaxMessage failure(String msg)
    {
        return create("failure", msg);
    }

    public static AjaxMessage failure()
    {
        return failure("操作失败");
    }

    public static AjaxMessage failure(String message, Exception ex)
    {
        if (ex == null) {
            return failure();
        }

        AjaxMessage result = failure(message);

        try {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            result.setData(sw.toString());
        } catch (Throwable throwable) {
        }

        return result;
    }

    public static AjaxMessage failure(Exception ex)
    {
        return failure(ex.getMessage(), ex);
    }
}
