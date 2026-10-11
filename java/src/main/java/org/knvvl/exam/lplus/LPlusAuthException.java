package org.knvvl.exam.lplus;

/**
 * Thrown when authentication against the LPlus TestStudio API fails.
 */
public class LPlusAuthException extends RuntimeException
{
    public LPlusAuthException(String message)
    {
        super(message);
    }

    public LPlusAuthException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
