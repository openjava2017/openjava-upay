package org.openjava.upay.rpc.exception;

public class SSLContextInitException extends ServiceAccessException
{
    public SSLContextInitException(String message)
    {
        super(message);
    }

    public SSLContextInitException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
