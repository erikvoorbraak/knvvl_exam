package org.knvvl.tools.generic.http;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class FileInfoBuilderImpl implements Request.FileInfo, Request.FileInfoBuilder
{
    private final Path path;
    private final byte[] bytes;
    private final String fileName;
    private String contentType;

    FileInfoBuilderImpl(@Nonnull byte[] bytes, @Nonnull String fileName)
    {
        this.path = null;
        this.bytes = requireNonNull(bytes);
        this.fileName = requireNonNull(fileName);
    }

    FileInfoBuilderImpl(@Nonnull Path path)
    {
        this.path = requireNonNull(path);
        this.bytes = null;
        this.fileName = path.getFileName().toString();
    }

    @Override
    public FileInfoBuilderImpl withContentType(@Nullable String contentType)
    {
        this.contentType = contentType;
        return this;
    }

    @Override
    public Request.FileInfo build()
    {
        return this;
    }

    @Nonnull
    @Override
    public String fileName()
    {
        return fileName;
    }

    @Nonnull
    @Override
    public String contentType()
    {
        return requireNonNullElseGet(contentType, () ->
            requireNonNullElse(MimeUtils.getContentType(fileName), Request.CONTENT_TYPE_OCTET_STREAM));
    }

    @Nonnull
    @Override
    public InputStream getInputStream() throws IOException
    {
        return bytes != null
            ? new ByteArrayInputStream(bytes)
            : Files.newInputStream(path);
    }
}
