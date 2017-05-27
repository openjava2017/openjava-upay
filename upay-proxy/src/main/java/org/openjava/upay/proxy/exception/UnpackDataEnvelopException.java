package org.openjava.upay.proxy.exception;

import org.openjava.upay.shared.type.ErrorCode;

public class UnpackDataEnvelopException extends Exception
{
    private int code = 1000;

    private boolean stackTrace = true;

    public UnpackDataEnvelopException(ErrorCode errorCode)
    {
        super(errorCode.getName());
        this.code = errorCode.getCode();
        this.stackTrace = false;
    }

    public UnpackDataEnvelopException(ErrorCode errorCode, Throwable cause)
    {
        super(cause);
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
