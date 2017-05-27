package org.openjava.upay.shared.exception;

public class UserTransactionException extends RuntimeException
{
    private static final int DEFAULT_CODE = -1;

    private int code = DEFAULT_CODE;
    private boolean stackTrace = true;

    public UserTransactionException()
    {
        super();
    }

    public UserTransactionException(String message)
    {
        super(message);
    }

    public UserTransactionException(String message, int code)
    {
        super(message);
        this.code = code;
    }

    public UserTransactionException(String message, boolean stackTrace)
    {
        super(message);
        this.stackTrace = stackTrace;
    }

    public UserTransactionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UserTransactionException(Throwable cause)
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
