package org.knvvl.tools.generic.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.knvvl.tools.generic.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class HttpRequestException extends HttpClientException
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestException.class);

    private final int statusCode;
    private final String url;
    protected final Object body;

    /**
     * Constructor with only message
     * 
     * @param url The url of the failed request
     * @param body The body with error message
     * @param statusCode The status code that came back from the server
     */
    protected HttpRequestException(String url, int statusCode, Object body)
    {
        super(constructMessage(url, statusCode));

        this.statusCode = statusCode;
        this.body = body;
        this.url = url;
    }

    public static HttpRequestException create(String url, HttpResponse<?> response)
    {
        Object body = response.body();
        int status = response.statusCode();
        if (body instanceof InputStream)
        {
            return new HttpRequestInputStreamException(url, status, (InputStream)body);
        }
        return new HttpRequestException(url, status, body);
    }

    private static String constructMessage(String url, int statusCode)
    {
        return String.format("Failed request %d: %s", statusCode, url);
    }

    /**
     * @return The status code
     */
    public int getStatusCode()
    {
        return statusCode;
    }

    /**
     * @return The body of the response converted to a UTF8 String 
     */
    @Nonnull
    public String getBody()
    {
        return body == null ? "" : body.toString().trim();
    }

    /**
     * @return The body in the type as converted by internal BodyHandler
     */
    @Nullable
    public Object getBodyHandlerResult()
    {
        return body;
    }

    /**
     * @return The body as bytes. The implementation does a best effort to return the bytes of the error response.
     */
    @Nonnull
    public byte[] getBodyBytes()
    {
        return getBody().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + "\n" + StringUtils.truncate(getBody(), 256);
    }

    /**
     * @return The url of the failed request
     */
    public String getUrl()
    {
        return url;
    }
    
    /**
     * Because a stream can not be read multiple times we convert it the bytes for the time being.
     */
    private static class HttpRequestInputStreamException extends HttpRequestException
    {
        HttpRequestInputStreamException(String url, int statusCode, InputStream stream)
        {
            super(url, statusCode, bytesFromStream(stream, url, statusCode));
        }

        @Nonnull
        @Override
        public String getBody()
        {
            return new String(getBodyBytes(), StandardCharsets.UTF_8);
        }

        @Nullable
        @Override
        public Object getBodyHandlerResult()
        {
            return new ByteArrayInputStream(getBodyBytes());
        }

        @Nonnull
        @Override
        public byte[] getBodyBytes()
        {
            return (byte[])body;
        }

        private static byte[] bytesFromStream(InputStream bodyStream, String url, int status)
        {
            try (InputStream stream = bodyStream)
            {
                return stream.readAllBytes();
            }
            catch (IOException e)
            {
                String message = String.format("Failed to read/close error stream for %s : %d", url, status);
                LOGGER.warn(message, e);
                throw new HttpClientException(message, e);
            }
        }
    }
}
