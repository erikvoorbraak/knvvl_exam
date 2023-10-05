package org.knvvl.exam.rest;

import static java.util.stream.Collectors.joining;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.knvvl.exam.ExamApplication;
import org.knvvl.exam.services.BackupService;
import org.knvvl.exam.services.ChangeDetector;
import org.knvvl.exam.services.DropboxService;
import org.knvvl.exam.spring.UserDetailsServiceImpl;
import org.knvvl.exam.spring.UserDetailsServiceImpl.UserLogon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api")
public class AdminRestService
{
    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
    private static final String ADMIN_PAGE = """
        <html><body>
        <h1>Admin Page</h1>
        Application started: {started}.<br/>
        Last data change: {lastChanged}.<br/>
        Number of changes since startup: {numChanges}.
        <h2>User Logons</h2>
        {userLogons}
        <h2>Download Backup</h2>
        Click <a href="/api/export" target="_blank">here</a> to download a Json backup.
        <h2>Restore Backup</h2>
        <form method="POST" action="/api/import" enctype="multipart/form-data">
        <input type="file" name="file" accept="application/json"/>
        <input type="submit" name="submit"/>
        </form>
        {dropboxService}
        <h2>About</h2>
        Created by Erik Voorbraak in 2023, to be used for KNVvL "Examencommissie".
        </body></html>""";

    @Autowired
    private BackupService backupService;
    @Autowired
    private ChangeDetector changeDetector;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private DropboxService dropboxService;

    @GetMapping(value = "/admin", produces = TEXT_HTML_VALUE)
    ResponseEntity<String> backupHtml()
    {
        var lastChanged = changeDetector.getLastChanged();
        var page = ADMIN_PAGE
            .replace("{started}", ExamApplication.getStartedInstant().toString())
            .replace("{lastChanged}", lastChanged == null ? "(no changes since startup)" : lastChanged.toString())
            .replace("{numChanges}", String.valueOf(changeDetector.getNumChanges()))
            .replace("{userLogons}", userDetailsService.getUserLogons().stream().map(UserLogon::toString).collect(joining("<br/>")))
            .replace("{dropboxService}", dropboxService.getAdminSectionHtml());
        return ResponseEntity.ok(page);
    }

    @GetMapping(value = "/export", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<String> exportBackup(HttpServletResponse response) throws IOException
    {
        Path file = createTimestampedFile("Export");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getFileName());
        backupService.exportAll(file);
        try (var stream = response.getOutputStream())
        {
            Files.copy(file, stream);
        }
        Files.delete(file);

        return ResponseEntity.ok("");
    }

    private static Path createTimestampedFile(String prefix)
    {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return Path.of(prefix + timestamp + ".json");
    }

    @PostMapping(value = "/exportDropbox", produces = TEXT_HTML_VALUE)
    ResponseEntity<String> exportToDropbox() throws IOException
    {
        Path file = createTimestampedFile("Export");
        backupService.exportAll(file);
        String message = dropboxService.exportNow(file);
        Files.delete(file);
        return ResponseEntity.ok(message);
    }

    @PostMapping(value = "/import", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> importBackup(@RequestParam("file") MultipartFile mpFile) throws IOException
    {
        if (!backupService.canRestore())
        {
            return ResponseEntity.status(BAD_REQUEST).body("Database must be empty before restore");
        }
        byte[] bytes = mpFile.getBytes();
        if (bytes.length == 0)
        {
            return ResponseEntity.status(BAD_REQUEST).body("No file to upload was found");
        }
        Path file = createTimestampedFile("Import");
        Files.write(file, bytes);
        var imported = backupService.importFrom(file);
        Files.delete(file);

        return ResponseEntity.ok(imported);
    }
}
