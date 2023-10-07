package org.knvvl.exam.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Strings;

@Service
public class GoogleCloudStorageService
{
    private static final String HTML = """
        <h2>Export to Google Cloud Storage</h2>
        Bucket name as configured in exam.google-cloud-storage.bucket-name: {bucket-name}.<br/>
        Last uploaded to Google Cloud Storage: {lastUpload}.<br/>
        {exportNow}
        """;
    private static final String EXPORT_NOW = """
        <form method="POST" action="/api/exportGoogleCloudStorage">
        <input type="submit" name="submit" value="Export Now"/>
        </form>""";
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStorageService.class);

    private String lastUploadToCloudStorage = "(not yet uploaded since application startup)";

    @Value("${exam.google-cloud-storage.bucket-name}")
    private String bucketName = "";

    @Autowired
    private ChangeDetector changeDetector;

    public boolean isConfigured()
    {
        return !Strings.isNullOrEmpty(bucketName);
    }

    public String getAdminSectionHtml()
    {
        return HTML
            .replace("{bucket-name}", bucketName)
            .replace("{lastUpload}", lastUploadToCloudStorage)
            .replace("{exportNow}", isConfigured() ? EXPORT_NOW : "");
    }

    public String exportNow(Path file)
    {
        var needsBackup = changeDetector.needsBackup();
        if (!needsBackup.needsBackup())
        {
            return needsBackup.message();
        }
        return lastUploadToCloudStorage = doExportNow(file);
    }

    private String doExportNow(Path file)
    {
        Storage storage = StorageOptions.getDefaultInstance().getService();

        Bucket bucket = storage.get(bucketName);
        try
        {
            String name = file.getFileName().toString();
            byte[] bytes = Files.readAllBytes(file);
            bucket.create(name, bytes);
            changeDetector.createdBackup();
            return "Exported to Google Cloud Storage: " + bucketName;
        }
        catch (IOException e)
        {
            LOGGER.error("Error reading file " + file);
            return "Error reading file " + file;
        }
    }
}
