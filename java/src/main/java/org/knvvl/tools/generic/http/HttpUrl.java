package org.knvvl.tools.generic.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.knvvl.tools.generic.EncodeUtils;

/**
 * <p>Simple builder to parse url's for query parameters or add them to an URL. This class takes care of the URL encoding when converted to the url.</p>
 * 
 * <pre>
 * HttpUrl url = HttpUrl.builder("http://localhost:8080/bwise?code=123&amp;test=TEST%3F").build();
   url.getQueryParam("code");  // returns 123
 * </pre>
 * 
 * <pre>
 * HttpUrl url = HttpUrl.builder("http://localhost:8080/bwise?code=123&amp;test=TEST%3F")
 *     .addQueryParam("new", "true")
 *     .build();
 * url.toUrl();   // The new url with the added param
 * </pre>
 * 
 * Also the fragment part is supported. Thats the part after the &#35; which is never logged and sent to the server. See example:
 * 
 * <pre>
 * HttpUrl url = HttpUrl.builder("http://localhost:8080/bwise&#35;code=123&amp;test=TEST%3F")
 *     .addFragmentParam("new", "true")
 *     .build();
 * url.toUrl();   // The new url with the added param
 * </pre>
 * 
 * <p>
 * NOTE: This implemenation removes the default ports from the url when possible.
 * </p>
 * @author gevmic0
 *
 */
public final class HttpUrl
{
    private static final String HTTPS = "https";
    private final URI uri;
    private final Map<String, List<String>> queryParams;
    private final Map<String, List<String>> fragmentParams;
    private final List<String> path;

    private HttpUrl(URI uri, List<String> path, Map<String, List<String>> queryParams, Map<String, List<String>> fragmentParams)
    {
        this.uri = uri;
        this.queryParams = queryParams;
        this.fragmentParams = fragmentParams;
        this.path = path;
    }

    /**
     * Get a query param by name
     * 
     * @param key The key
     * @return The first value or null when no values found.
     */
    @Nullable
    public String getQueryParam(String key)
    {
        List<String> values = getQueryParams(key);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    /**
     * Get multiple query parameter by name if there are multiple
     * 
     * @param key The key
     * @return The list of values for this key
     */
    public List<String> getQueryParams(@Nonnull String key)
    {
        Objects.requireNonNull(key, "Key cannot be null");

        return queryParams.get(key);
    }

    /**
     * Get a fragment parameter by name
     * 
     * @param key The key
     * @return The first value or null when no values found.
     */
    @Nullable
    public String getFragmentParam(@Nonnull String key)
    {
        Objects.requireNonNull(key, "Key cannot be null");

        List<String> values = getFragmentParams(key);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    /**
     * @param key The key
     * @return The list of values for this key
     */
    public List<String> getFragmentParams(@Nonnull String key)
    {
        Objects.requireNonNull(key, "Key cannot be null");

        return fragmentParams.get(key);
    }
    
    /**
     * @return The path of the url
     */
    @Nonnull
    public String getPath()
    {
        return "/" + String.join("/", path);
    }

    /**
     * @return Checks if this url is secure (SSL). Uses the https protocol.
     */
    public boolean isSecure()
    {
        return HTTPS.equalsIgnoreCase(getScheme());
    }

    /**
     * 
     * @return The scheme of this url. Like http or https.
     */
    public String getScheme()
    {
        return uri.getScheme();
    }

    /**
     * @return The host in this url
     */
    public String getHost()
    {
        return uri.getHost();
    }

    private URI toUri()
    {
        try
        {
            return new URI(getScheme(), uri.getUserInfo(), getHost(), getNormalizedPort(), getPath(), getQuery(), getFragment());
        }
        catch (URISyntaxException e)
        {
            throw new HttpClientException("Invalid URL: " + uri, e);
        }
    }

    private int getNormalizedPort()
    {
        int port = getPort();
        if (isSecure() && port == 443)
        {
            return -1;
        }
        else if (!isSecure() && port == 80)
        {
            return -1;
        }
        else
        {
            return port;
        }
    }

    /*
     * The port as specified
     * @return The port as specified
     */
    public int getPort()
    {
        return uri.getPort();
    }

    private String getQuery()
    {
        return queryParams.isEmpty() ? null : EncodeUtils.urlEncodeMultiParams(queryParams);
    }

    private String getFragment()
    {
        return fragmentParams.isEmpty() ? null : EncodeUtils.urlEncodeMultiParams(fragmentParams);
    }

    /**
     * The url will never contain a port when not needed
     * 
     * @return The string version of the build url
     */
    @Nonnull
    public String toUrl()
    {
        return toUri().toASCIIString().replace("%25", "%");  // Unknown why this is needed.
    }

    /**
     * Same as toUrl()
     */
    @Override
    public String toString()
    {
        return toUrl();
    }

    /**
     * Create a new builder with the specified string
     * 
     * @param urlStr Url String
     * @return The Builder
     */
    @Nonnull
    public static Builder builder(String urlStr)
    {
        return new Builder(urlStr);
    }

    
    /**
     * Create a new builder with the specified HttpUrl
     * 
     * @param url HttpUrl
     * @return The Builder
     */
    @Nonnull
    public static Builder builder(HttpUrl url)
    {
        return new Builder(url.toUrl());
    }

    public static Builder builder(String scheme, String host, int port, String context)
    {
        try
        {
            return new Builder(new URI(scheme, null, host, port, context, null, null));
        }
        catch (URISyntaxException e)
        {
            throw new HttpClientException("Invalid input", e);
        }
    }

    public static final class Builder
    {
        private final URI uri;
        private final Map<String, List<String>> queryParams = new LinkedHashMap<>();   // Keep order
        private final Map<String, List<String>> fragmentParams = new LinkedHashMap<>();   // Keep order
        private final List<String> path = new ArrayList<>();

        private Builder(String urlStr)
        {
            this(URI.create(urlStr));
        }

        private Builder(URI uri)
        {
            this.uri = uri;

            addToQueryParams(uri.getRawQuery());

            String fragment = uri.getRawFragment();
            if (fragment != null)
            {
                for (String param : fragment.split("&"))
                {
                    String[] pair = param.split("=");
                    addFragmentParam(decode(pair[0]), pair.length > 1 ? decode(pair[1]) : "");
                }
            }

            addToPath(uri.getRawPath());
        }

        private void addToQueryParams(String query)
        {
            if (query != null)
            {
                for (String param : query.split("&"))
                {
                    String[] pair = param.split("=");
                    addQueryParam(decode(pair[0]), pair.length > 1 ? decode(pair[1]) : "");
                }
            }
        }

        private void addToPath(String pathStr)
        {
            // Remove optional ending /
            path.remove("");  //NOSONAR

            path.addAll(Arrays.stream(pathStr.split("/"))
                .filter(s -> !s.isBlank()).collect(Collectors.toList()));

            if (pathStr.endsWith("/")) 
            {
                path.add("");  // Add / at the end
            }
        }

        /**
         * @return The build HttpUrl
         */
        @Nonnull
        public HttpUrl build()
        {
            return new HttpUrl(uri, path, queryParams, fragmentParams);
        }

        /**
         * Add a new query parameter. The query is the part after the ?.
         * 
         * @param key Key
         * @param value Value
         * @return The Builder
         */
        @Nonnull
        public Builder addQueryParam(@Nonnull String key, @Nonnull String value)
        {
            Objects.requireNonNull(key, "Key cannot be null");
            Objects.requireNonNull(value, "Value cannot be null");

            queryParams.computeIfAbsent(key, v -> new ArrayList<>()).add(value);
            return this;
        }

        /**
         * Add a new fragment parameter. The fragment is the part after the &#35; sign and is never logged or sent to the server.
         * 
         * @param key Key
         * @param value Value
         * @return The Builder
         */
        @Nonnull
        public Builder addFragmentParam(@Nonnull String key, @Nonnull String value)
        {
            Objects.requireNonNull(key, "Key cannot be null");
            Objects.requireNonNull(value, "Value cannot be null");

            fragmentParams.computeIfAbsent(key, v -> new ArrayList<>()).add(value);
            return this;
        }

        /**
         * Add a new path parameter. This also supports appending a path with query parameters and multiple paths separated with /'s.
         * 
         * @param pathStr Path
         * @return The Builder
         */
        @Nonnull
        public Builder addPathParam(@Nonnull String pathStr)
        {
            Objects.requireNonNull(pathStr, "Path cannot be null");

            String[] parts = pathStr.split("[?]");
            if (parts.length > 0)
            {
                addToPath(parts[0]);
            }
            if (parts.length > 1)
            {
                addToQueryParams(parts[1]);
            }
            return this;
        }
    }

    private static String decode(String value)
    {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
