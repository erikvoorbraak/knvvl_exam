package org.knvvl.exam.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.google.common.base.Strings;

@Service
public class DropboxService
{
    private static final String HTML = """
        <h2>Dropbox Upload</h2>
        Dropbox access token is configured as exam.dropbox.access-token: {configured}.<br/>
        Last uploaded to Dropbox: {lastUpload}.<br/>
        {uploadNow}
        """;
    private static final String UPLOAD_NOW = """
        <form method="POST" action="/api/exportDropbox">
        <input type="submit" name="submit" value="Upload Now"/>
        </form>""";

    @Value("${exam.dropbox.access-token}")
    String dropboxAccessToken = "";

    private String lastUploadToDropbox = "(not yet uploaded since application startup)";

    public String getAdminSectionHtml()
    {
        boolean isConfigured = !Strings.isNullOrEmpty(dropboxAccessToken);
        return HTML
            .replace("{configured}", isConfigured ? "Yes" : "No")
            .replace("{lastUpload}", lastUploadToDropbox)
            .replace("{uploadNow}", isConfigured ? UPLOAD_NOW : "");
    }

    public String exportNow(Path file)
    {
        return lastUploadToDropbox = doExportNow(file);
    }

    private String doExportNow(Path file)
    {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("knvvl/exam-upload").build();
        DbxClientV2 client = new DbxClientV2(config, dropboxAccessToken);

        String accountName;
        try
        {
            accountName = client.users().getCurrentAccount().getName().getDisplayName();
        }
        catch (DbxException e)
        {
            return "Cannot access Dropbox account: " + e.getMessage();
        }

        try (InputStream in = new FileInputStream(file.toFile()))
        {
            var fileMetadata = client.files().uploadBuilder("/" + file.getFileName()).uploadAndFinish(in);
            return "Uploaded to Dropbox of " + accountName + ": " + fileMetadata.getName();
        }
        catch (IOException e)
        {
            return "Cannot read from file: " + e.getMessage();
        }
        catch (DbxException e)
        {
            return "Error uploading file to Dropbox: " + e.getMessage();
        }
    }
}
