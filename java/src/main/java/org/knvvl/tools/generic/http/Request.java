package org.knvvl.tools.generic.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.knvvl.tools.generic.http.RequestBuilder.Method;

/**
 * <p>Request API that can be used to sent request to the server using the {@link SimpleHttpClient}</p>
 *
 * <p>Instance of Request can be build using the builder like:</p>
 *
 * <pre>
 *     Request req = Request.buildPost().withPath("api").withFormParam("label", "Value").build();
 *     new SimpleHttpClient().sendForString(req);
 * </pre>
 *
 * @author gevmic0
 */
public interface Request
{
    enum FormContentType
    {
        X_WWW_FORM_ENCODED,
        MULTIPART_FORM_DATA,
        NOT_A_FORM
    }

    Charset CHARSET = StandardCharsets.UTF_8;

    String CONTENT_TYPE_JSON = "application/json";
    String CONTENT_TYPE_XML = "application/xml; charset=" + CHARSET.name();
    String CONTENT_TYPE_TEXT = "text/plain; charset=" + CHARSET.name();
    String CONTENT_TYPE_ZIP = "application/zip";
    String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
    String CONTENT_TYPE_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    String CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data; boundary=%s";

    String HEADER_CONTENT_TYPE = "Content-Type";
    String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    String HEADER_ACCEPT = "accept";
    String HEADER_AUTHORIZATION = "Authorization";

    String BEARER_PREFIX = "Bearer ";
    
    /**
     * Captures information about a file to be added as form parameter
     */
    interface FileInfo
    {
        static FileInfoBuilder builder(@Nonnull Path file)
        {
            return new FileInfoBuilderImpl(file);
        }

        static FileInfoBuilder builder(@Nonnull byte[] bytes, @Nonnull String fileName)
        {
            return new FileInfoBuilderImpl(bytes, fileName);
        }

        @Nonnull
        String fileName();

        @Nonnull
        String contentType();

        @Nonnull
        InputStream getInputStream() throws IOException;
    }

    interface FileInfoBuilder
    {
        FileInfoBuilder withContentType(@Nullable String contentType);

        FileInfo build();
    }

    /**
     * Builder interface to build Request objects
     *
     * @author gevmic0
     */
    interface Builder
    {
        /**
         * Validate and build the final request object
         *
         * @return The request
         */
        Request build();

        /**
         * By default this path will be (smart) appended to the context url of the client. One
         * exception is when the path starts with http then that path will be used.
         *
         * @param path The path which will be appended to the context
         * @return The builder
         */
        Builder withPath(@Nonnull String path);

        /**
         * @param key   The form parameter
         * @param value The value
         * @return The builder
         */
        Builder withFormParam(@Nonnull String key, @Nonnull String value);

        /**
         * Invokes withFormParam from a map
         *
         * @param params Form params as a map
         * @return The builder
         */
        Builder withFormParams(@Nonnull Map<String, String> params);

        /**
         * Invokes withQueryParams from a map
         *
         * @param key   Query parameter name
         * @param value Query value
         * @return The builder
         */
        Builder withQueryParam(@Nonnull String key, @Nonnull String value);

        /**
         * @param params Multiple query parameters as a Map
         * @return The builder
         */
        Builder withQueryParams(@Nonnull Map<String, String> params);

        /**
         * @param key   Header key
         * @param value Header value
         * @return The builder
         */
        Builder withHeader(@Nonnull String key, @Nonnull String value);

        /**
         * Sets the body payload as a string
         *
         * @param body The body payload
         * @return The builder
         */
        Builder withBody(@Nonnull String body);

        /**
         * Sets the body payload as a byte array
         *
         * @param bytes The body payload in bytes
         * @return The builder
         */
        Builder withBody(@Nonnull byte[] bytes);

       /**
         * Sets the body payload as a stream supplier
         *
         * @param streamSupplier The body payload as stream supplier
         * @return The builder
         */
        Builder withBody(@Nonnull Supplier<InputStream> streamSupplier);

        /**
         * Sets the body payload to be taken from a file. 
         * Also sets the content-length and content-type header based on the Path.
         *
         * @param path The body payload as a file path
         * @return The builder
         */
        Builder withBody(@Nonnull Path path);

        /**
         * Set the body payload and the content-type header to application/json
         *
         * @param body The json string
         * @return The builder
         */
        Builder withJsonBody(@Nonnull String body);

        /**
         * Sets the content-type header
         *
         * @param contentType The content type
         * @return The builder
         */
        Builder withContentType(@Nonnull String contentType);

        /**
         * Sets the accept header
         *
         * @param accept The accept header value. Might be a content type.
         * @return The builder
         */
        Builder withAccept(@Nonnull String accept);

        /**
         * Specifies to sent this request using the multipart form data protocol
         *
         * @return The builder
         */
        Builder asMultipartFormData();

        /**
         * Add a file to the form. This will also enable the multipart form data protocol.
         *
         * @param key  The form field name
         * @param file Path to a file
         * @return The builder
         */
        Builder withFormParam(@Nonnull String key, @Nonnull Path file);

        /**
         * Add a file with a certain content type to the form. This will also enable the multipart form data protocol.
         *
         * @param key         The form field name
         * @param fileInfo    Request.FileInfo instance
         * @return The builder
         */
        Builder withFormParam(@Nonnull String key, @Nonnull FileInfo fileInfo);

        /**
         * Add a file to the form. This will also enable the multipart form data protocol.
         *
         * @param key       The form field name
         * @param fileBytes Byte array containing the file bytes.
         * @param fileName  The filename
         * @return The builder
         */
        Builder withFormParam(@Nonnull String key, @Nonnull byte[] fileBytes, @Nonnull String fileName);
    }

    /**
     * Build a POST request
     *
     * @return The builder
     */
    static Builder buildPost()
    {
        return new RequestBuilder(Method.POST);
    }

    /**
     * Build a POST request
     *
     * @return The builder
     */
    static Builder buildGet()
    {
        return new RequestBuilder(Method.GET);
    }

    /**
     * Build a PUT request
     *
     * @return The builder
     */
    static Builder buildPut()
    {
        return new RequestBuilder(Method.PUT);
    }

    /**
     * Build a DELETE request
     *
     * @return The builder
     */
    static Builder buildDelete()
    {
        return new RequestBuilder(Method.DELETE);
    }

    /**
     * Build a PATCH request
     *
     * @return The builder
     */
    static Builder buildPatch()
    {
        return new RequestBuilder(Method.PATCH);
    }

    /**
     * @return The method of this request
     */
    Method getMethod();

    /**
     * Never null;
     *
     * @return The path of this request or an empty string. The path is relative to the contextUrl specified in the SimpleHttpClient.
     */
    @Nonnull
    String getPath();

    /**
     * @return How the body is supposed to be encoded
     */
    FormContentType getFormContentType();

    /**
     * @return Get the body as a stream
     */
    Supplier<InputStream> getBodyStreamSupplier();

    /**
     * @return multipart form data boundary if content type is MULTIPART_FORM_DATA
     */
    @Nullable
    String getMultipartFormDataBoundary();

    /**
     * @return the length in bytes of the body when available. Otherwise -1.
     */
    long getBodyLength();

    /**
     * @return The map of headers
     */
    Map<String, String> getHeaders();

    /**
     * @return The form params
     */
    Map<String, List<String>> getFormParams();

    /**
     * @return The form file infos
     */
    Map<String, List<FileInfo>> getFormFileInfos();

    /**
     * @return The query params
     */
    Map<String, List<String>> getQueryParams();
}
