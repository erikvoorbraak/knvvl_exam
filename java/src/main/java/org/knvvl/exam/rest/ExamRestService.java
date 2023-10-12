package org.knvvl.exam.rest;

import static org.knvvl.exam.rest.QuestionRestService.GSON;
import static org.knvvl.exam.services.Utils.getAsString;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.services.ExamCreationService;
import org.knvvl.exam.services.ExamException;
import org.knvvl.exam.services.ExamRepositories;
import org.knvvl.exam.services.ExamService;
import org.knvvl.exam.services.ExamToPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("api")
public class ExamRestService
{
    @Autowired private ExamRepositories examRepositories;
    @Autowired private ExamCreationService examCreationService;
    @Autowired private ExamService examService;
    @Autowired private ExamToPdfService examToPdfService;
    @Autowired private QuestionRestService questionRestService;

    @GetMapping(value = "/exams", produces = APPLICATION_JSON_VALUE)
    String getExams()
    {
        JsonArray all = new JsonArray();
        for (Exam.ExamView exam : examRepositories.getExamRepository().getExamByOrderByIdDesc()) // Newest first
        {
            int examId = exam.getId();
            JsonObject json = new JsonObject();
            json.addProperty("id", examId);
            json.addProperty("label", exam.getLabel());
            json.addProperty("certificate", exam.getCertificate());
            json.addProperty("language", exam.getLanguage());
            json.addProperty("fileSize", exam.getFileSize());
            json.addProperty("url", "/api/exams/" + examId);
            all.add(json);
        }
        return GSON.toJson(all);
    }

    @GetMapping(value = "/exams/{examId}", produces = APPLICATION_JSON_VALUE)
    String getExam(@PathVariable("examId") int examId)
    {
        Exam exam = examRepositories.getExamRepository().getReferenceById(examId);
        JsonObject json = new JsonObject();
        json.addProperty("id", examId);
        json.addProperty("label", exam.getLabel());
        json.addProperty("certificate", exam.getCertificate());
        json.addProperty("language", exam.getLanguage());
        List<ExamQuestion> questions = examService.getQuestionsForExam(examId);
        json.addProperty("questions", printQuestions(questions, q -> String.valueOf(q.getId())));
        json.addProperty("answers", printQuestions(questions, Question::getAnswer));
        return GSON.toJson(json);
    }

    private String printQuestions(List<ExamQuestion> questions, Function<Question, String> function)
    {
        StringBuilder b = new StringBuilder();
        Topic topic = questions.get(0).getTopic();
        for (ExamQuestion question : questions)
        {
            if (topic != question.getTopic())
            {
                topic = question.getTopic();
                b.append("\n");
            }
            b.append(function.apply(question.getQuestion()));
            b.append(",");
        }
        String result = b.toString();
        return result.isEmpty() ? "" : result.substring(0, result.length() - 1); // Minus last comma
    }

    @PostMapping(path = "exams", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createExam(@RequestBody String body)
    {
        Map<String, String> json = GSON.fromJson(body, Map.class);
        String label = json.get("label");
        if (StringUtils.isBlank(label))
            return ResponseEntity.status(BAD_REQUEST).body("Label is mandatory");
        String certificate = json.get("certificate");
        if (StringUtils.isBlank(certificate))
            return ResponseEntity.status(BAD_REQUEST).body("Certificate is mandatory");
        String language = json.get("language");
        if (StringUtils.isBlank(language))
            return ResponseEntity.status(BAD_REQUEST).body("Language is mandatory");

        Exam exam = examRepositories.getExamRepository().findByLabel(label);
        if (exam != null)
            return ResponseEntity.status(BAD_REQUEST).body("Exam with this name already exists");

        try {
            examCreationService.createExam(label, Integer.parseInt(certificate), language);
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
        return ResponseEntity.status(OK).body(null);
    }

    @PostMapping(path = "exams/{examId}", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateExam(@PathVariable("examId") int examId, @RequestBody String body)
    {
        Exam exam = examRepositories.getExamRepository().getReferenceById(examId);
        JsonObject form = GSON.fromJson(body, JsonObject.class);
        String label = getAsString(form, "label");
        if (!label.isBlank())
            exam.setLabel(label);
        examRepositories.getExamRepository().save(exam);
        return ResponseEntity.status(OK).body(null);
    }

    @PostMapping(value = "/exams/{examId}/pdf", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> uploadExamPdf(@PathVariable("examId") int examId, @RequestParam("file") MultipartFile file) throws IOException
    {
        Exam exam = examRepositories.getExamRepository().getReferenceById(examId);
        exam.setFilePdf(file.getBytes());
        examRepositories.getExamRepository().save(exam);
        return ResponseEntity.status(OK).body(null);
    }

    @GetMapping(value = "/exams/{examId}/pdf", produces = APPLICATION_PDF_VALUE)
    byte[] getExamPdf(@PathVariable("examId") int examId)
    {
        Exam exam = examRepositories.getExamRepository().getReferenceById(examId);
        return exam.getFilePdf();
    }

    @DeleteMapping(path = "exams/{examId}")
    private void deleteExam(@PathVariable("examId") int examId)
    {
        examService.deleteExam(examId);
    }

    @GetMapping(value = "/exams/{examId}/questions", produces = APPLICATION_JSON_VALUE)
    String getQuestionsForExam(@PathVariable(name = "examId") int examId)
    {
        JsonArray all = new JsonArray();
        for (ExamQuestion examQuestion : examRepositories.getExamQuestionRepository().findByExamOrderByQuestionIndex(examId))
        {
            JsonObject json = questionRestService.getJsonQuestion(examQuestion.getQuestion(), false);
            json.addProperty("examQuestionId", examQuestion.getId());
            all.add(json);
        }
        return GSON.toJson(all);
    }

    @GetMapping(path = "/exams/altquestions/{examQuestionId}", produces = APPLICATION_JSON_VALUE)
    String getAltQuestions(@PathVariable String examQuestionId)
    {
        JsonArray all = new JsonArray();
        ExamQuestion examQuestion = examRepositories.getExamQuestionRepository().getReferenceById(Integer.parseInt(examQuestionId));
        for (Question altQuestion : examService.getAltQuestions(examQuestion))
        {
            JsonObject json = questionRestService.getJsonQuestion(altQuestion, false);
            json.addProperty("url", "/api/exams/altquestions/" + examQuestionId);
            all.add(json);
        }
        return GSON.toJson(all);
    }

    @PostMapping(path = "/exams/altquestions/{examQuestionId}", consumes = TEXT_PLAIN_VALUE)
    void setAltQuestion(@PathVariable String examQuestionId, @RequestBody String altQuestionId)
    {
        examService.setAltQuestion(Integer.parseInt(examQuestionId), Integer.parseInt(altQuestionId));
    }

    @GetMapping(value = "/exams/{examId}/generate", produces = APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> generatePdf(@PathVariable("examId") int examId,
        @RequestParam(value = "withQuestionId", defaultValue = "false") boolean withQuestionId,
        HttpServletResponse response)
    {
        try (var stream = response.getOutputStream())
        {
            Exam exam = examRepositories.getExamRepository().getReferenceById(examId);
            String filename = exam.getLabel() + ".pdf";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            byte[] bytes = examToPdfService.generatePdf(exam, withQuestionId);
            stream.write(bytes);
            return ResponseEntity.status(OK).build();
        }
        catch (ExamException | IOException e)
        {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(e.getMessage().getBytes());
        }
    }
}
