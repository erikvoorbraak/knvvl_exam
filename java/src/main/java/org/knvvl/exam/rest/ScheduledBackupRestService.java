package org.knvvl.exam.rest;

import static org.knvvl.exam.rest.AdminRestService.createTimestampedFile;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.knvvl.exam.services.BackupService;
import org.knvvl.exam.services.GoogleCloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;

@RestController
@RequestMapping("backup")
public class ScheduledBackupRestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledBackupRestService.class);

    @Autowired
    private BackupService backupService;
    @Autowired
    private GoogleCloudStorageService googleCloudStorageService;

    @Value("${exam.backup.access-token}")
    private String backupAccessToken;

    @PostMapping(value = "", produces = TEXT_PLAIN_VALUE)
    ResponseEntity<String> exportBackup(@RequestHeader("backupAccessToken") String accessToken)
    {
        boolean supportsToken = !Strings.isNullOrEmpty(backupAccessToken);
        boolean receivedToken = !Strings.isNullOrEmpty(accessToken);
        boolean authorized = supportsToken && backupAccessToken.equals(accessToken);
        LOGGER.info("Backup to Google Cloud Storage invoked" +
            ", supportsToken=" + supportsToken + ", receivedToken=" + receivedToken + ", authorized=" + authorized);
        if (!authorized)
        {
            return ResponseEntity.status(UNAUTHORIZED).build();
        }
        try
        {
            Path file = createTimestampedFile("Export");
            backupService.exportAll(file);
            var message = googleCloudStorageService.exportNow(file);
            LOGGER.info(message);
            Files.delete(file);
            return ResponseEntity.ok(message);
        }
        catch (Throwable e)
        {
            LOGGER.error("Error exporting", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
