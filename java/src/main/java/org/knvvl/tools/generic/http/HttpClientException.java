package org.knvvl.tools.generic.http;

/**
 * Generic exception for the SimpleHttpClient
 * 
 * @author gevmic0
 *
 */
@SuppressWarnings("serial")
public class HttpClientException extends RuntimeException
{
    /**
     * Constructor with only message
     * 
     * @param message The message
     */
    public HttpClientException(String message)
    {
        super(message);
    }

    /**
     * Constructor with message and cause
     * 
     * @param message The message
     * @param cause The cause
     */
    public HttpClientException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
