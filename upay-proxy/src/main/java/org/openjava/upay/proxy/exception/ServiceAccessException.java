package org.openjava.upay.proxy.exception;

import org.openjava.upay.shared.type.ErrorCode;

public class ServiceAccessException extends RuntimeException
{
    private int code = ErrorCode.UNKNOWN_EXCEPTION.getCode();

    private boolean stackTrace = true;

    public ServiceAccessException(ErrorCode errorCode)
    {
        this(errorCode.getName(), errorCode.getCode());
    }

    public ServiceAccessException(String message)
    {
        super(message);
        this.stackTrace = false;
    }

    public ServiceAccessException(String message, int code)
    {
        super(message);
        this.code = code;
        this.stackTrace = false;
    }

    public ServiceAccessException(String message, Throwable cause)
    {
        super(message, cause);
    }

    @Override
    public Throwable fillInStackTrace()
    {
        return stackTrace ? super.fillInStackTrace() : this;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }
}
