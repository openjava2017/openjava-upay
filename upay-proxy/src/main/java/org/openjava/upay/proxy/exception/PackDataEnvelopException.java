package org.openjava.upay.proxy.exception;

import org.openjava.upay.shared.type.ErrorCode;

public class PackDataEnvelopException extends Exception
{
    private int code = ErrorCode.UNKNOWN_EXCEPTION.getCode();

    private boolean stackTrace = true;

    public PackDataEnvelopException(ErrorCode errorCode)
    {
        super(errorCode.getName());
        this.code = errorCode.getCode();
        stackTrace = false;
    }

    public PackDataEnvelopException(ErrorCode errorCode, Throwable cause)
    {
        super(errorCode.getName(), cause);
        this.code = errorCode.getCode();
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
