package org.knvvl.tools.generic.http;

import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.knvvl.tools.generic.EncodeUtils;

class RequestBuilder implements Request.Builder, Request
{
    // enum values are used as present here when generating request
    enum Method
    {
        GET, POST, PUT, DELETE, PATCH
    }

    private final Method method;
    private String path = "";  // Never null
    private FormContentType bodyContentType = FormContentType.NOT_A_FORM;
    private String multipartFormDataBoundary;

    private final Map<String, List<String>> formParams = new LinkedHashMap<>();
    private final Map<String, List<FileInfo>> formFileInfos = new LinkedHashMap<>();
    private final Map<String, List<String>> queryParams = new LinkedHashMap<>();
    private final Map<String, String> headers = new LinkedHashMap<>();

    private Supplier<InputStream> bodySupplier = null;
    private long bodyLength = -1;

    RequestBuilder(Method method)
    {
        this.method = requireNonNull(method);
    }

    @Override
    public Request build()
    {
        if (hasFormData() && bodySupplier != null)
        {
            throw new HttpClientException("Combining form parameters and a body is not possible");
        }

        switch (requireNonNull(getFormContentType()))
        {
            case MULTIPART_FORM_DATA:
                bodySupplier = new MultipartBodySupplier(this);
                break;
            case X_WWW_FORM_ENCODED:
                withBody(EncodeUtils.urlEncodeMultiParams(getFormParams()));
                break;
            case NOT_A_FORM:
                bodySupplier = (bodySupplier == null) ? () -> new ByteArrayInputStream(new byte[0]) : bodySupplier;
                break;
        }
        return this;
    }

    @Override
    public Builder withPath(@Nonnull String path)
    {
        this.path = requireNonNull(path);
        return this;
    }

    @Override
    public Builder withFormParam(@Nonnull String key, @Nonnull String value)
    {
        setDefaultContentTypeForForm();
        List<String> values = formParams.computeIfAbsent(key, k -> new ArrayList<>());
        values.add(requireNonNull(value));
        return this;
    }

    @Override
    public Builder withBody(@Nonnull String body)
    {
        withBody(body.getBytes(CHARSET));
        return this;
    }

    @Override
    public Builder withBody(@Nonnull byte[] bytes)
    {
        bodyLength = bytes.length;
        this.bodySupplier = () -> new ByteArrayInputStream(bytes);
        return this;
    }

    @Override
    public Builder withBody(@Nonnull Path path)
    {
        bodyLength = getFileSize(path);

        withBody(() -> getStream(path));

        String type = MimeUtils.getContentType(path.getFileName().toString());
        if (type != null)
        {
            withContentType(type);
        }
        return this;
    }

    private static long getFileSize(Path file)
    {
        try
        {
            return Files.size(file);
        }
        catch (IOException e)
        {
            throw new HttpClientException("Failed to get file size from:" + file, e);
        }
    }

    private static InputStream getStream(Path file)
    {
        try
        {
            return new BufferedInputStream(Files.newInputStream(file));
        }
        catch (IOException e)
        {
            throw new HttpClientException("Failed to read file:" + file, e);
        }
    }

    @Override
    public Builder withBody(@Nonnull Supplier<InputStream> streamSupplier)
    {
        this.bodySupplier = streamSupplier;
        return this;
    }
    
    @Override
    public @Nonnull Supplier<InputStream> getBodyStreamSupplier()
    {
        return bodySupplier;
    }

    @Override
    @Nonnull
    public String getPath()
    {
        return path;
    }

    @Override
    public Map<String, String> getHeaders()
    {
        return headers;
    }

    @Override
    public Map<String, List<String>> getFormParams()
    {
        return formParams;
    }

    @Override
    public Builder withQueryParam(@Nonnull String key, @Nonnull String value)
    {
        queryParams.computeIfAbsent(key, k -> new ArrayList<>()).add(requireNonNull(value));
        return this;
    }

    @Override
    public Method getMethod()
    {
        return method;
    }

    @Override
    public Map<String, List<String>> getQueryParams()
    {
        return queryParams;
    }

    @Nullable
    @Override
    public FormContentType getFormContentType()
    {
        return bodyContentType;
    }

    @Nullable
    @Override
    public String getMultipartFormDataBoundary()
    {
        return multipartFormDataBoundary;
    }

    @Override
    public Builder withHeader(@Nonnull String key, @Nonnull String value)
    {
        headers.put(key, requireNonNull(value));
        return this;
    }

    @Override
    public Builder withJsonBody(@Nonnull String body)
    {
        withBody(body);
        withContentType(CONTENT_TYPE_JSON);
        return this;
    }

    @Override
    public Builder withContentType(@Nonnull String contentType)
    {
        withHeader(HEADER_CONTENT_TYPE, requireNonNull(contentType));
        return this;
    }

    @Override
    public Builder withFormParams(@Nonnull Map<String, String> params)
    {
        params.forEach(this::withFormParam);
        return this;
    }

    @Override
    public Builder withQueryParams(@Nonnull Map<String, String> params)
    {
        params.forEach(this::withQueryParam);
        return this;
    }

    @Override
    public Builder asMultipartFormData()
    {
        setFormContentType(FormContentType.MULTIPART_FORM_DATA);
        return this;
    }

    @Override
    public Builder withFormParam(@Nonnull String key, @Nonnull Path file)
    {
        return withFormParam(key, FileInfo.builder(file).build());
    }

    @Override
    public Builder withFormParam(@Nonnull String key, @Nonnull FileInfo fileInfo)
    {
        asMultipartFormData();
        formFileInfos.computeIfAbsent(key, k -> new ArrayList<>()).add(fileInfo);
        return this;
    }

    @Override
    public Builder withFormParam(@Nonnull String key, @Nonnull byte[] fileBytes, @Nonnull String fileName)
    {
        asMultipartFormData();

        formFileInfos.computeIfAbsent(key, k -> new ArrayList<>()).add(FileInfo.builder(fileBytes, fileName).build());
        return this;
    }

    private void setFormContentType(FormContentType newBodyContentType)
    {
        requireNonNull(newBodyContentType, "bodyContentType cannot be null");
        if (newBodyContentType.equals(this.bodyContentType))
        {
            return; // No change
        }
        this.bodyContentType = newBodyContentType;
        if (newBodyContentType == FormContentType.MULTIPART_FORM_DATA)
        {
            multipartFormDataBoundary = "##" + System.currentTimeMillis();
            withContentType(String.format(CONTENT_TYPE_MULTIPART_FORM, multipartFormDataBoundary));
        }
        else
        {
            multipartFormDataBoundary = null;
            if (newBodyContentType == FormContentType.X_WWW_FORM_ENCODED)
            {
                withContentType(CONTENT_TYPE_X_WWW_FORM_URLENCODED);
            }
        }
    }

    private void setDefaultContentTypeForForm()
    {
        if (bodyContentType == FormContentType.NOT_A_FORM)
        {
            setFormContentType(FormContentType.X_WWW_FORM_ENCODED);
        }
    }

    private boolean hasFormData()
    {
        return !formParams.isEmpty() || !formFileInfos.isEmpty();
    }

    @Override
    public Map<String, List<FileInfo>> getFormFileInfos()
    {
        return formFileInfos;
    }

    @Override
    public long getBodyLength()
    {
        return bodyLength;
    }

    @Override
    public Builder withAccept(@Nonnull String accept)
    {
        withHeader(HEADER_ACCEPT, requireNonNull(accept));
        return this;
    }
}
