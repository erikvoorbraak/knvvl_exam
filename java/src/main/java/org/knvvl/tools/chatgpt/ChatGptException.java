package org.knvvl.tools.chatgpt;

import org.knvvl.tools.generic.http.HttpRequestException;

/**
 * Exception related to chatgpt
 */
public class ChatGptException extends RuntimeException
{
    public ChatGptException(String message, Throwable e)
    {
        super(message, e);
    }

    public ChatGptException(String message)
    {
        super(message);
    }

    public static ChatGptException from(RuntimeException exception)
    {
        String message = exception.getMessage();
        if (exception instanceof HttpRequestException ex)
        {
            message = Json.parseError(ex.getBody()).message(); 
        }
        return new ChatGptException(message, exception);   
    }
}
