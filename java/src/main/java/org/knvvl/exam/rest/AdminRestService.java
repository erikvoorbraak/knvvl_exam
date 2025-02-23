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
import org.knvvl.exam.services.ExamAnswersImportService;
import org.knvvl.exam.services.GoogleCloudStorageService;
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
        Click <a href="/api/exportBackup" target="_blank">here</a> to download a Json backup.
        <h2>Restore Backup</h2>
        <form method="POST" action="/api/importBackup" enctype="multipart/form-data">
        <input type="file" name="file" accept="application/json"/>
        <input type="submit" name="submit"/>
        </form>
        
        <h2>Import answers</h2>
        See <a href="/api/admin/formats">formats</a> page for file formats.<br/>
        Select tab-delimited JSON file:
        <form method="POST" action="/api/importAnswersJson" enctype="multipart/form-data">
        <input type="file" name="file" accept="application/json"/>
        <input type="submit" name="submit"/>
        </form>
        Select tab-delimited TXT file:
        <form method="POST" action="/api/importAnswersTxt" enctype="multipart/form-data">
        <input type="file" name="file" accept="text/plain"/>
        <input type="submit" name="submit"/>
        </form>

        {googleCloudStorageService}
        <h2>About</h2>
        Created by Erik Voorbraak in 2023, to be used for KNVvL "Examencommissie".
        </body></html>""";

    private static final String FORMATS_PAGE = """
        <html><body>
        <h2>Import answers as JSON</h2>
        Contents is a JSON array of JSON objects:
        <ul>
        <li>"student": identifies the person taking the exam.</li>
        <li>"exam": a single integer number representing the exam's database ID.</li>
        <li>"question": integer value local to topic, eg. 1, 2, ..., 20.</li>
        <li>"topic": represents database ID of the topic, eg 1, 2, ..., 5.</li>
        <li>"answerCorrect": can be multiple uppercase characters.</li>
        <li>"answerGiven": must be a single uppercase character.</li>
        </ul>
        Example JSON file:
        <pre>
        [
          {
            "student": "123456",
            "exam": 123,
            "question": 20,
            "topic": 5,
            "answerCorrect": "AB",
            "answerGiven": "D"
          },
          etc
        ]
        </pre>
        
        <h2>Import answers as TXT</h2>
        Three placeholder must be in the file as single lines. Additional blank lines are allowed.<br/>
        <ul>
        <li>ExamId - next line must be a single integer number representing the exam's database ID.</li>
        <li>CorrectAnswers - next lines are tab delimited: first entry is "topic id" and the rest are the expected correct answers.</li>
        <li>GivenAnswers - next lines are tab delimited: first entry is "student id", second is "topic id", rest are answers given.</li>
        </ul>
        It is allowed to have multiple exams in the same file; each occurrence of "ExamId" will reset "CorrectAnswers".<br/>
        Example tab-delimited TXT file:
        <pre>
        ExamId
        117
        
        CorrectAnswers
        1	AB	D	D	D	C	A	C	C	B	B	A	D	A	B	B	D	D	D	A	B
        2	BCD	B	C	D	D	B	B	A	D	B	D	D	C	A	A	A	B	B	A	A
        3	C	A	C	D	B	D	A	A	B	B	B	A	A	B	C	D	A	A	B	D
        4	C	A	D	B	D	B	C	B	C	B	B	C	D	D	D	A	D	B	D	C
        5	A	B	B	B	B	A	A	A	D	B	B	A	B	C	C	C	D	D	B	C
        
        GivenAnswers
        123456	3	A	A	C	D	B	D	B	A	C	C	B	A	A	B	C	D	A	A	B	D
        123456	2	D	B	C	D	D	B	B	A	D	C	D	D	C	A	A	A	B	B	A	A
        123456	5	A	B	B	B	D	A	A	D	D	B	B	A	B	B	C	C	B	D	B	D
        123456	4	C	A	D	B	D	D	C	B	C	B	B	C	D	D	D	A	D	B	D	C
        123456	1	A	D	D	D	C	D	C	C	B	D	A	D	A	B	B	D	D	D	A	B
        234567	2	C	B	C	D	D	C	B	A	B	B	D	D	x	A	x	A	D	B	B	D
        </pre>
        </body></html>""";

    @Autowired
    private BackupService backupService;
    @Autowired
    private ExamAnswersImportService examAnswersImportService;
    @Autowired
    private ChangeDetector changeDetector;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private GoogleCloudStorageService googleCloudStorageService;

    @GetMapping(value = "/admin", produces = TEXT_HTML_VALUE)
    ResponseEntity<String> getAdminPage()
    {
        var lastChanged = changeDetector.getLastChanged();
        var page = ADMIN_PAGE
            .replace("{started}", ExamApplication.getStartedInstant().toString())
            .replace("{lastChanged}", lastChanged == null ? "(no changes since startup)" : lastChanged.toString())
            .replace("{numChanges}", String.valueOf(changeDetector.getNumChanges()))
            .replace("{userLogons}", userDetailsService.getUserLogons().stream().map(UserLogon::toString).collect(joining("<br/>")))
            .replace("{googleCloudStorageService}", googleCloudStorageService.getAdminSectionHtml());
        return ResponseEntity.ok(page);
    }

    @GetMapping(value = "/admin/formats", produces = TEXT_HTML_VALUE)
    ResponseEntity<String> getFormatsPage()
    {
        return ResponseEntity.ok(FORMATS_PAGE);
    }

    @GetMapping(value = "/exportBackup", produces = APPLICATION_JSON_VALUE)
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

    static Path createTimestampedFile(String prefix)
    {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return Path.of(prefix + timestamp + ".json");
    }

    @PostMapping(value = "/importBackup", produces = TEXT_PLAIN_VALUE)
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

    @PostMapping(value = "/exportGoogleCloudStorage", produces = TEXT_HTML_VALUE)
    ResponseEntity<String> exportToGoogleCloudStorage() throws IOException
    {
        Path file = createTimestampedFile("Export");
        backupService.exportAll(file);
        String message = googleCloudStorageService.exportNow(file);
        Files.delete(file);
        return ResponseEntity.ok(message);
    }

    @PostMapping(value = "/importAnswersJson", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> importAnswersJson(@RequestParam("file") MultipartFile mpFile) throws IOException
    {
        byte[] bytes = mpFile.getBytes();
        if (bytes.length == 0)
        {
            return ResponseEntity.status(BAD_REQUEST).body("No file to upload was found");
        }
        try
        {
            Path file = createTimestampedFile("Import");
            Files.write(file, bytes);
            var imported = examAnswersImportService.importFromJson(file);
            Files.delete(file);
            return ResponseEntity.ok(imported);
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping(value = "/importAnswersTxt", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> importAnswersTxt(@RequestParam("file") MultipartFile mpFile) throws IOException
    {
        byte[] bytes = mpFile.getBytes();
        if (bytes.length == 0)
        {
            return ResponseEntity.status(BAD_REQUEST).body("No file to upload was found");
        }
        try
        {
            Path file = createTimestampedFile("Import");
            Files.write(file, bytes);
            var imported = examAnswersImportService.importFromTabDelimited(file);
            Files.delete(file);
            return ResponseEntity.ok(imported);
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
