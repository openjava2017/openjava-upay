package org.openjava.upay.core.exception;

import org.openjava.upay.shared.type.ErrorCode;

public class FundTransactionException extends RuntimeException
{
    private int code = 0;

    private boolean stackTrace = true;

    public FundTransactionException()
    {
        super();
    }

    public FundTransactionException(ErrorCode errorCode)
    {
        this(errorCode.getName(), errorCode.getCode());
    }

    public FundTransactionException(String message)
    {
        super(message);
        this.stackTrace = false;
    }

    public FundTransactionException(String message, int code)
    {
        super(message);
        this.code = code;
        this.stackTrace = false;
    }

    public FundTransactionException(String message, boolean stackTrace)
    {
        super(message);
        this.stackTrace = stackTrace;
    }

    public FundTransactionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public FundTransactionException(Throwable cause)
    {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace()
    {
        return stackTrace ? super.fillInStackTrace() : this;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }
}
