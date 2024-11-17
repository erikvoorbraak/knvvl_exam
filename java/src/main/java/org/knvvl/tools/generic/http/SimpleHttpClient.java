package org.knvvl.tools.generic.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.knvvl.tools.generic.EncodeUtils;
import org.knvvl.tools.generic.crypto.SslUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Simple http(s) client that uses the JDK HttpClient and tries to make it more easy and less error prone to use.</p>
 * 
 * <p>It is suggested to sub class when you want to implement some specific authentication. See below for an example:</p>
 * 
 * <pre>
 *       class ClientWithAuth extends SimpleHttpClient
 *       {
 *           ClientWithAuth(String uid, String pwd)
 *           {
 *               super("http://localhost//bwise");
 *
 *               String session = postForString("/login", Map.of("uid", uid, "pwd", pwd));
 *               setDefaultHeaders(Map.of("session", session));
 *           }
 *       }
 * </pre>
 * 
 * <p>Then to authenticate and invoke a GET request you can use the following example:</p>
 * 
 * <pre>
 *     ClientWithAuth client = new ClientWithAuth("Admin", "Welcome01");
 *     client.getForString("/api/test");  // This will now also sent the session header with the retrieved value
 * </pre>
 * 
 * <p>For posting form data the following can be used</p>
 *
 * <pre>
 *     SimpleHttpClient client = new SimpleHttpClient("https://localhost/bwise");
 *     client.postForString("/api", Map.of("label", "Hello World")));
 * </pre>
 * 
 * <p>For other cases the Request builder can be used, which will return the body of the response in case of success</p>
 * 
 * <pre>
 *     SimpleHttpClient client = new SimpleHttpClient("https://localhost/bwise");
 *     client.sendForString(Request.buildPost()
 *         .withPath("/api")
 *         .withFormParam("label", "Hello")
 *         .withHeader("header", "value")
 *         .build());
 * </pre>
 *
 * <p>Using send with Request, will return the HttpResponse&lt;String&gt;</p>
 *
 * <pre>
 *     SimpleHttpClient client = new SimpleHttpClient("https://localhost/bwise");
 *     client.send(Request.buildPost()
 *         .withPath("/api")
 *         .withFormParam("label", "Hello")
 *         .withHeader("header", "value")
 *         .build());
 * </pre>
 *
 * <p>The client is also able to sent form data using the multipart protocol</p>
 * 
 * <pre>
 *     SimpleHttpClient client = new SimpleHttpClient("https://localhost/bwise");
 *     client.sendForString(Request.buildPost()
 *         .asMultipart()
 *         .withFormParam("label", "Hello")
 *         .build());
 * </pre>
 * 
 * <p>
 * The request path will be (smart) appended to the context path of the client except
 * when the path starts with http.
 * </p>
 * @author gevmic0
 */
public class SimpleHttpClient
{
    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
        Pattern.compile("(password=)[^&]*"),
        Pattern.compile("(email=)[^&]*"),
        Pattern.compile("(secret=)[^&]*"),
        Pattern.compile("(pwd=)[^&]*")
    );
    private static final String REPLACEMENT = "$1****";
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClient.class);

    private final HttpClient httpClient;
    private final String contextUrl;
    private final Config config;
    private final Map<String, String> defaultHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Construct a client with the contextUrl which is the prefix for all urls used later on.
     * 
     * Like https://myserver.com/api
     * 
     * @param contextUrl The context url. All paths in the request will be appended to this one.
     */
    public SimpleHttpClient(String contextUrl)
    {
        this(contextUrl, Config.builder().build());
    }

    /**
     * Here you can enable to trust all certificated and ignore all warnings.
     * Only do this in test situations.
     * 
     * @param contextUrl The contextUrl like http://server/bwise
     * @param ignoreSslWarnings Put only to false during testing
     * @deprecated Use {@link #SimpleHttpClient(String, Config)}
     */
    @Deprecated
    public SimpleHttpClient(String contextUrl, boolean ignoreSslWarnings)
    {
        this(contextUrl, Config.builder().withIgnoreSslWarnings(ignoreSslWarnings).build());
    }

    /**
     * Here you can enable to trust all certificated and ignore all warnings.
     * Only do this in test situations.
     * 
     * @param contextUrl The contextUrl like http://server/bwise
     * @param config Config object
     */
    public SimpleHttpClient(String contextUrl, Config config)
    {
        this.contextUrl = Objects.requireNonNull(contextUrl, "SimpleHttpClient needs a contextUrl");

        HttpClient.Builder builder = HttpClient.newBuilder()
            .followRedirects(config.followRedirects() ? Redirect.NORMAL : Redirect.NEVER)
            .version(Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(30));
        if (config.ignoreSslWarnings())
        {
            builder = builder.sslContext(SslUtil.getTrustAllContext());
        }
        if (config.supportCookies())
        {
            builder = builder.cookieHandler(new CookieManager());
        }

        httpClient = builder.build();

        this.config = config;
    }

    /**
     * Send a POST request with a payload in the body of the request.
     * When the path starts with http then that path will be used.
     * 
     * @param path The path which will be added to the contextUrl provided in the constructor
     * @param payload The body content
     * @param contentType The type of the body content
     * @return The response as a string
     * @throws HttpRequestException when the status is not in the 200-299 range
     * @throws HttpClientException Other issues
     */
    public String postForString(String path, String payload, String contentType)
    {
        return sendForString(Request.buildPost().withPath(path).withBody(payload).withContentType(contentType).build());
    }

    /**
     * Send a request that is build using the Request builder
     * 
     * @param request A request
     * @return The body of response as a string, but only if response status is 200-299
     * otherwise an exception will be thrown
     * @throws HttpRequestException when the status is not in the 200-299 range
     * @throws HttpClientException Other issues
     */
    public String sendForString(Request request)
    {
        return sendRequest(getRequestBuilder(request).build(), BodyHandlers.ofString()).body();
    }

    /**
     * Send a request that is build using the Request builder. The stream should be closed by the invoker from this method.
     *
     * @param request A request
     * @return The body of response as an inputStream, but only if response status is 200-299
     * otherwise an exception will be thrown
     * @throws HttpRequestException when the status is not in the 200-299 range
     * @throws HttpClientException Other issues
     */
    public InputStream sendForStream(Request request)
    {
        return sendRequest(getRequestBuilder(request).build(), BodyHandlers.ofInputStream()).body();
    }

    /**
     * This gives you full access to the underlying HttpResponse. Often useful for tests.
     * This will always return the response and will not throw exceptions on specific result statuses.
     *
     * @param request A request that is build using the Request builder
     * @return The httpResponse of the underlying jdk http client
     * @throws HttpClientException Other issues
     */
    public HttpResponse<String> send(Request request)
    {
        return send(request, BodyHandlers.ofString());
    }

    /**
     * This gives you full access to the underlying HttpResponse. Often useful for tests.
     * This will always return the response and will not throw exceptions on specific result statuses.
     * NOTE: Using this method will not throw HttpRequestException for none 200 status codes.
     *
     * @param request A request that is build using the Request builder
     * @param responseBodyHandler Handler for the expected response
     * @return The httpResponse of the underlying jdk http client
     * @throws HttpClientException in case of issues
     */
    public <T> HttpResponse<T> send(Request request, HttpResponse.BodyHandler<T> responseBodyHandler)
    {
        HttpRequest httpRequest = getRequestBuilder(request).build();
        try
        {
            return httpClient.send(httpRequest, responseBodyHandler);
        }
        catch (IOException e)
        {
            throw new HttpClientException(createFailedMessage(httpRequest, e), e);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new HttpClientException(createFailedMessage(httpRequest, e), e);
        }
    }

    /**
     * Send a POST request with the specified form parameters as form encoded string
     * When the path starts with http then that path will be used.
     * 
     * @param path The path which will be added to the contextUrl provided in the constructor
     * @param formParams The form parameters as a Map
     * @return The response as a String
     * @throws HttpRequestException when the status is not in the 200-299 range
     * @throws HttpClientException Other issues
     */
    public String postFormForString(String path, Map<String, String> formParams)
    {
        return sendForString(Request.buildPost().withPath(path).withFormParams(formParams).build());
    }

    /**
     * <p>Send a GET request and return the data as a string</p>
     * <p>When the path starts with http then that path will be used.</p>
     * 
     * @param path The path which will be added to the contextUrl provided in the constructor
     * @return The response as a string
     * @throws HttpRequestException when the status is not in the 200-299 range
     * @throws HttpClientException Other issues
     */
    public String getForString(String path)
    {
        return sendForString(Request.buildGet().withPath(path).build());
    }

    /**
     * <p>Send a GET request and return the data as a string</p>
     * <p>
     * This is the same as</p>
     * <pre>
     * getForString("")
     * </pre>
     * <p>
     * And so will invoke a GET request to the context url specified in the constructor
     * </p>
     * @return The response as a string
     * @throws HttpRequestException when the status is not in the 200-299 range
     * @throws HttpClientException Other issues
     */
    public String getForString()
    {
        return sendForString(Request.buildGet().build());
    }

    /**
     * <p>Send a POST request with JSON and return the data as a string</p>
     * 
     * @param json The json that will be sent to the server
     * @return The response as a string
     * @throws HttpRequestException when the status is not in the 200-299 range
     * @throws HttpClientException Other issues
     */
    public String postJsonForString(String json)
    {
        return sendForString(Request.buildPost().withJsonBody(json).build());
    }

    private HttpRequest.Builder getRequestBuilder(Request request)
    {
        beforeEachRequest(request);

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(getFullUrl(request)));

        config.getFixedHeaders().forEach(builder::setHeader);

        defaultHeaders.forEach(builder::setHeader);

        request.getHeaders().forEach(builder::setHeader);  // Override header from default

        RequestBuilder.Method method = request.getMethod();
        switch (method)
        {
            case DELETE:
                return builder.DELETE();
            case GET:
                return builder.GET();
            case PATCH:
            case POST:
            case PUT:
                return builder.method(method.name(), getPublisher(request));
            default:
                throw new HttpClientException("Invalid method:" + method);
        }
    }

    private BodyPublisher getPublisher(Request request)
    {
        BodyPublisher publisher = BodyPublishers.ofInputStream(request.getBodyStreamSupplier());
        long length = request.getBodyLength();
        return length >= 0 ? new BodyPublisherWithLength(publisher, length) : publisher;
    }

    private <T> HttpResponse<T> sendRequest(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
    {
        return sendRequest(request, responseBodyHandler, 1);
    }

    private <T> HttpResponse<T> sendRequest(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, int retryCount)
    {
        try
        {
            HttpResponse<T> resp = httpClient.send(request, responseBodyHandler);
            if (resp.statusCode() >= 200 && resp.statusCode() < 300)  // Success
            {
                if (LOGGER.isTraceEnabled())
                {
                    String sanitizedResponse = sanitizeResponse(resp);
                    LOGGER.trace("Request: {}", sanitizedResponse);
                    LOGGER.trace("Response: {}", resp.body());
                }
                return resp;
            }
            else if (resp.statusCode() == 503 && retryCount <= getConfig().getMaxNumberOfRetries())   // Server not available (Overloaded of starting)
            {
                // Sometimes servers are not available for a short time because they are overloaded. Then retry max the configured times.
                LOGGER.warn("Server Not Available. Try again ({}/{}).", retryCount, getConfig().getMaxNumberOfRetries());
                Thread.sleep(1000);
                retryCount++;
                return sendRequest(request, responseBodyHandler, retryCount);
            }
            else
            {
                String sanitizedUrl = sanitizeUrl(request.uri().toString());
                HttpRequestException ex = HttpRequestException.create(sanitizedUrl, resp);
                LOGGER.warn("Request failed: {}", sanitizedUrl);
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("    Headers: {}", resp.headers().map());
                    LOGGER.debug("    Response: {}", ex.getMessage());
                }
                throw ex;
            }
        }
        catch (IOException e)
        {
            throw new HttpClientException(createFailedMessage(request, e), e);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new HttpClientException(createFailedMessage(request, e), e);
        }
    }

    private String createFailedMessage(HttpRequest request, Throwable e)
    {
        return "Failed to send data to url: " + request.uri() + improveMessage(e);
    }

    private String improveMessage(Throwable cause)
    {
        if (cause == null || cause.getMessage() == null)
        {
            return "";
        }

        String message = cause.getMessage();
        if (message.contains("PKIX"))
        {
            message = "Failed to verify the SSL certificate. Try enabling ignoreSslWarnings for testing.";
        }

        return " : " + message;
    }

    /**
     * Sets new headers and cleans the old one first. For example after login.
     * 
     * @param defaultHeaders Map of headers to add
     */
    protected void setDefaultHeaders(Map<String, String> defaultHeaders)
    {
        clearDefaultHeaders();

        this.defaultHeaders.putAll(defaultHeaders);
    }

    /**
     * Clears the default headers. For example after logout.
     */
    protected void clearDefaultHeaders()
    {
        defaultHeaders.clear();
    }

    /**
     * @param name The header name case-insensitive
     * @return The header value
     */
    protected String getDefaultHeader(String name)
    {
        return defaultHeaders.get(name);
    }

    /**
     * Here subclasses can implement logic that should happen before each request. Like authentication.
     * @param request The request
     */
    protected void beforeEachRequest(Request request)
    {
        // Here subclasses can implement logic that should happen before each request. Like authentication.
    }

    /**
     * @return Config object from constructor
     */
    protected Config getConfig()
    {
        return config;
    }

    private String getFullUrl(String path)
    {
        if (path != null && !path.isBlank())
        {
            // If path by it selves already contains a full url then use that one.
            return path.startsWith("http") ? path : HttpUrl.builder(contextUrl).addPathParam(path).build().toUrl();
        }
        return contextUrl;
    }

    /**
     * @param request The request
     * @return The full url of this provided request.
     */
    public String getFullUrl(Request request)
    {
         String path = getFullUrl(request.getPath());
         if (request.getQueryParams().isEmpty())
         {
             return path;
         }
         else
         {
             String query = EncodeUtils.urlEncodeMultiParams(request.getQueryParams());
             return path + (path.contains("?") ? "&" : "?") + query;
         }
    }

    /**
     * Sanitizes the input string by replacing sensitive information based on predefined patterns.
     *
     * @param input The input string to be sanitized.
     * @return The sanitized string with sensitive information replaced.
     */
    protected static String sanitize(String input)
    {
        if (input == null)
        {
            return null;
        }
        for (Pattern pattern : SENSITIVE_PATTERNS)
        {
            input = pattern.matcher(input).replaceAll(REPLACEMENT);
        }
        return input;
    }

    /**
     * Sanitizes the given URL by replacing sensitive information.
     *
     * @param url The URL to be sanitized.
     * @return The sanitized URL with sensitive information replaced.
     */
    protected String sanitizeUrl(String url)
    {
        return sanitize(url);
    }

    /**
     * Sanitizes the given HTTP response by replacing sensitive information.
     *
     * @param response The HTTP response to be sanitized.
     * @return The sanitized response as a string with sensitive information replaced.
     */
    protected String sanitizeResponse(HttpResponse<?> response)
    {
        return sanitize(response.toString());
    }

    /**
     * <p>Config interface to tweak this client a bit.</p>
     * <p>You implement this interface or use the builder via createDefault().</p>
     * <pre>
     * Config config = Config.createDefault().withFollowRedirects(false).build();
     * </pre>
     * 
     * @author gevmic0
     */
    public interface Config
    {
        /**
         * @return If the client should support tracking cookies
         */
        boolean supportCookies();

        /**
         * @return If the client should following redirect automatically or not
         */
        boolean followRedirects();

        /**
         * Sometimes useful for testing with man in the middle security tools on you environment. 
         * This can also work around for invalid untrusted certificates.
         * The certificate warning will be logged.
         * 
         * @return If the client should ignore SSL warnings. 
         */
        boolean ignoreSslWarnings();

        /**
         * When the client returns a 503 (Server not available) it will retry the request for this number of times.
         * @return Max number of retries 
         */
        int getMaxNumberOfRetries();
        
        /**
         * @return The headers that will always be sent during every request
         */
        Map<String, String> getFixedHeaders();
        
        /**
         * @return Create a builder with default settings
         */
        static ConfigBuilder builder()
        {
            return new ConfigBuilder();
        }
    }
}
