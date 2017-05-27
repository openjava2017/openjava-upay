package org.openjava.upay.shared.exception;

public class CacheSystemException extends Exception
{
    private static final long serialVersionUID = 8994433942652287047L;

    public CacheSystemException(String msg)
    {
        super(msg);
    }
    
    public CacheSystemException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
