package org.knvvl.tools.generic.http;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * 
 * This class is able to create a multipart request body. The stream can be created by invoking the get method of the Stream Supplier interface
 * which is implemented by this class. Every invocation of the get method creates a fresh stream with the index on the start.
 * 
 * Inspired by:  https://www.codejava.net/java-se/networking/upload-files-by-sending-multipart-request-programmatically
 * 
 * @author gevmic0
 *
 */
class MultipartBodySupplier implements Supplier<InputStream>
{
    private static final String LINE_FEED = "\r\n";
    private static final String BOUNDARY_PREFIX = "--";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private ByteArrayOutputStream currentStream;
    private PrintWriter currentWriter;

    private final Request request;
    private final String boundary;
    private List<InputStream> streams = new ArrayList<>();

    /**
     * Default constructor
     */
    MultipartBodySupplier(Request request)
    {
        this.request = request;
        this.boundary = request.getMultipartFormDataBoundary();
    }

    private void fullReset()
    {
        streams.clear();
        currentStream = null;
        currentWriter = null;
        
        addAndReset();
    }

    private void build()
    {
        fullReset();
        request.getFormParams().forEach((k, lv) -> lv.forEach(v -> addFormField(k, v)));
        request.getFormFileInfos().forEach((k, lv) -> lv.forEach(v -> addFormFileInfo(k, v)));
        addFooter();
    }

    private void addAndReset()
    {
        if (currentStream != null)
        {
            currentWriter.close();
            streams.add(new ByteArrayInputStream(currentStream.toByteArray()));
        }

        currentStream = new ByteArrayOutputStream();
        currentWriter = new PrintWriter(currentStream, true, CHARSET);
    }

    /**
     * Adds a form field to the request
     * @param name field name
     * @param value field value
     */
    private void addFormField(String name, String value)
    {
        addBoundary();
        addHeaderField(Request.HEADER_CONTENT_DISPOSITION, String.format("form-data; name=\"%s\"", name));
        addHeaderField(Request.HEADER_CONTENT_TYPE, Request.CONTENT_TYPE_TEXT);
        addLineFeed();
        addLine(value);
        addAndReset();
    }

    private void addFormFileInfo(String name, Request.FileInfo fileInfo)
    {
        try
        {
            addFormStream(name, fileInfo.getInputStream(), fileInfo.fileName(), fileInfo.contentType());
        }
        catch (IOException e)
        {
            throw new HttpClientException("Failed to add stream for " + name + " and " + fileInfo.fileName(), e);
        }
    }

    private void addFormStream(@Nonnull String name, @Nonnull InputStream stream, @Nonnull String fileName, @Nonnull String contentType)
    {
        addBoundary();
        addHeaderField(Request.HEADER_CONTENT_DISPOSITION, String.format("form-data; name=\"%s\"; filename=\"%s\"", name, fileName));
        addHeaderField(Request.HEADER_CONTENT_TYPE, contentType);
        addHeaderField(Request.HEADER_CONTENT_TRANSFER_ENCODING, "binary");
        addLineFeed();
        addAndReset();

        streams.add(stream);

        addLineFeed();
        addAndReset();
    }

    private void addBoundary()
    {
        addLine(BOUNDARY_PREFIX + boundary);
    }

    private void addLineFeed()
    {
        addLine("");
    }

    private void addLine(String line)
    {
        currentWriter.append(line).append(LINE_FEED);
    }

    /**
     * Adds a header field to the request.
     * @param name - mandatory name of the header field
     * @param value - mandatory value of the header field
     */
    private void addHeaderField(@Nonnull String name, @Nonnull String value)
    {
        addLine(requireNonNull(name, "Header name") + ": " + requireNonNull(value, "Header value"));
    }

    private void addFooter()
    {
        addLine(BOUNDARY_PREFIX + boundary + BOUNDARY_PREFIX);
        addAndReset();
    }

    @Override
    public InputStream get()
    {
        build();  // Make sure every time you get a fresh stream with the index on the start.
        return new SequenceInputStream(Collections.enumeration(streams));
    }
}
