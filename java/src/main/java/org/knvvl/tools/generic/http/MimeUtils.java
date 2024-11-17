package org.knvvl.tools.generic.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Simple implementation of a conversion between filename and content type.
 * Get the implementation of GeneralLib or get a better one.
 * It might be that this works better when running inside of tomcat.
 * 
 * @author gevmic0
 *
 */
public final class MimeUtils
{
    private static final Map<String, String> extToContentTypeFallback = Map.of(
        "yml", "application/yaml",
        "yaml", "application/yaml"
        );

    private MimeUtils()
    {
    }

    /**
     * Get the contentType or a file extension. Or null when not found.
     * Using the first approach as described on https://www.baeldung.com/java-file-mime-type.
     * 
     * @param fileName File name with extension
     * @return The content type or null when not found
     */
    public static @Nullable String getContentType(String fileName)
    {
        try
        {
            String contentType = Files.probeContentType(Path.of(fileName));
            if (contentType == null)
            {
                int lastDot = fileName.lastIndexOf(".");
                if (lastDot == -1)
                {
                    return null;
                }
                String ext = fileName.substring(lastDot + 1).toLowerCase();
                contentType = extToContentTypeFallback.get(ext);
            }
            return contentType;
        }
        catch (IOException e)
        {
            throw new HttpClientException("Failed to get content type for: " + fileName, e);
        }
    }
}
