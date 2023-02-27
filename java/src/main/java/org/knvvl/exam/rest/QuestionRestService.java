package org.knvvl.exam.rest;

import static org.knvvl.exam.services.ExamRepositories.SORT_BY_ID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.knvvl.exam.entities.Change;
import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.services.ExamRepositories;
import org.knvvl.exam.services.QuestionField;
import org.knvvl.exam.services.QuestionService;
import org.knvvl.exam.services.QuestionService.QuestionCreateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("api")
public class QuestionRestService
{
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Autowired private ExamRepositories examRepositories;
    @Autowired private QuestionService questionService;

    @GetMapping(value = "/questions", produces = "application/json")
    String getQuestions(
        @RequestParam(name = "topic", defaultValue = "0") Integer topicId,
        @RequestParam(name = "requirement", defaultValue = "0") Integer requirementId,
        @RequestParam(name = "exam", defaultValue = "0") Integer examId,
        @RequestParam(name = "search", defaultValue = "") String search)
    {
        JsonArray all = new JsonArray();
        questionService.queryQuestions(SORT_BY_ID.descending(), topicId, requirementId, examId, search)
            .map(q -> this.getJsonQuestion(q, true))
            .forEach(all::add);
        return GSON.toJson(all);
    }

    @GetMapping(value = "/questions/{questionId}", produces = APPLICATION_JSON_VALUE)
    String getQuestion(@PathVariable("questionId") int questionId)
    {
        Question question = examRepositories.getQuestionRepository().getReferenceById(questionId);
        JsonObject json = getJsonQuestion(question, true);
        json.addProperty("topicId", question.getTopic().getId());
        json.addProperty("requirementId", question.getRequirement().getId());
        return GSON.toJson(json);
    }

    JsonObject getJsonQuestion(Question question, boolean addDetails)
    {
        JsonObject json = new JsonObject();
        json.addProperty("id", question.getId());
        for (QuestionField questionField : questionService.getQuestionFields())
        {
            questionField.writeJson(question, json);
            if (!addDetails && "answer".equals(questionField.getField()))
                return json;
        }
        json.addProperty("tagsHtml", getTags(question));
        return json;
    }

    private static String getTags(Question question)
    {
        List<String> keywords = new ArrayList<>();
        if (question.isAllowB2()) keywords.add("B2");
        if (question.isAllowB3()) keywords.add("B3");
        if (question.isIgnore()) keywords.add("Negeren");
        if (question.isDiscuss()) keywords.add("Bespreken");

        String examGroup = question.getExamGroup();
        if (!StringUtils.isBlank(examGroup))
            keywords.add(examGroup);

        Picture picture = question.getPicture();
        if (picture != null)
            keywords.add("<a target=\"_blank\" href=\"/api/pictures/" + picture.getId() + "\">" + picture.getFilename() + "</a>");
        return String.join(", ", keywords);
    }

    @PostMapping(path = "questions", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createQuestion(@RequestBody String body)
    {
        JsonObject form = GSON.fromJson(body, JsonObject.class);
        QuestionCreateResult result = questionService.createQuestion(form);
        if (result.message() != null)
            return ResponseEntity.status(BAD_REQUEST).body(result.message());
        return ResponseEntity.status(OK).body(null);
    }

    @PostMapping(path = "questions/{questionId}", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateQuestion(@PathVariable("questionId") int questionId, @RequestBody String body)
    {
        JsonObject form = GSON.fromJson(body, JsonObject.class);
        String message = questionService.updateQuestion(questionId, form);
        if (message != null)
            return ResponseEntity.status(BAD_REQUEST).body(message);
        return ResponseEntity.status(OK).body(null);
    }

    @GetMapping(value = "/questions/{questionId}/changes", produces = APPLICATION_JSON_VALUE)
    public String getChanges(@PathVariable("questionId") int questionId)
    {
        JsonArray all = new JsonArray();
        questionService.getChanges(questionId).stream().map(QuestionRestService::toJson).forEach(all::add);
        return GSON.toJson(all);
    }

    private static JsonObject toJson(Change change)
    {
        JsonObject json = new JsonObject();
        Change.ChangeKey changeKey = change.getChangeKey();
        ZonedDateTime zonedDateTime = changeKey.getChangedAt().atZone(ZoneId.systemDefault());
        json.addProperty("questionId", changeKey.getQuestion().getId());
        json.addProperty("changedBy", changeKey.getChangedBy().getUsername());
        json.addProperty("changedAt", zonedDateTime.format(DateTimeFormatter.ofPattern("d MMM uuuu H:mm")));
        json.addProperty("changedAtEpoch", changeKey.getChangedAt().getEpochSecond());
        json.addProperty("field", changeKey.getField());
        json.addProperty("oldValue", change.getOldValue());
        json.addProperty("newValue", change.getNewValue());
        return json;
    }
}
