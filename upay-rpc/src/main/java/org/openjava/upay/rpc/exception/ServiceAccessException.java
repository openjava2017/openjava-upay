package org.openjava.upay.rpc.exception;

public class ServiceAccessException extends Exception
{
    private static final long serialVersionUID = 8075758871840410195L;

    public ServiceAccessException(String message)
    {
        super(message);
    }
    
    public ServiceAccessException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
