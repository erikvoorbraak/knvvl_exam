package org.knvvl.exam.rest;

import static org.knvvl.exam.services.ExamRepositories.SORT_BY_ID;
import static org.knvvl.exam.services.Languages.LANGUAGE_NL;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.knvvl.exam.entities.Change;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.services.ExamRepositories;
import org.knvvl.exam.services.Languages;
import org.knvvl.exam.services.QuestionService;
import org.knvvl.exam.services.UserService;
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
    static final Map<Integer, String> userToFilterLanguage = new HashMap<>();

    @Autowired private ExamRepositories examRepositories;
    @Autowired private QuestionService questionService;
    @Autowired private UserService userService;

    @GetMapping(value = "/questions", produces = "application/json")
    String getQuestions(
        @RequestParam(name = "language", defaultValue = "") String language,
        @RequestParam(name = "topic", defaultValue = "0") Integer topicId,
        @RequestParam(name = "requirement", defaultValue = "0") Integer requirementId,
        @RequestParam(name = "exam", defaultValue = "0") Integer examId,
        @RequestParam(name = "search", defaultValue = "") String search)
    {
        JsonArray all = new JsonArray();
        questionService.queryQuestions(SORT_BY_ID.descending(), handleFilterLanguage(language), topicId, requirementId, examId, search)
            .map(q -> this.getJsonQuestion(q, true, false, true, false))
            .forEach(all::add);
        return GSON.toJson(all);
    }

    private String handleFilterLanguage(String language) {
        Integer userId = userService.getCurrentUser().getId();
        if (Languages.ALL.equals(language)) {
            userToFilterLanguage.put(userId, "");
            return "";
        }
        if (!isNullOrEmpty(language)) {
            userToFilterLanguage.put(userId, language);
            return language;
        }
        return getCurrentUserFilterLanguage();
    }

    @GetMapping(value = "/questions/filter/language", produces = TEXT_PLAIN_VALUE)
    String getCurrentUserFilterLanguage()
    {
        return userToFilterLanguage.getOrDefault(userService.getCurrentUser().getId(), LANGUAGE_NL.id());
    }

    @GetMapping(value = "/questions/{questionId}", produces = APPLICATION_JSON_VALUE)
    String getQuestion(@PathVariable("questionId") int questionId)
    {
        var question = examRepositories.getQuestionRepository().getReferenceById(questionId);
        var json = getJsonQuestion(question, true, true, false, true);
        return GSON.toJson(json);
    }

    @GetMapping(value = "/questions/{questionId}/translated", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<String> getQuestionTranslated(@PathVariable("questionId") int questionId)
    {
        var question = examRepositories.getQuestionRepository().getReferenceById(questionId);
        var error = questionService.checkCanTranslate(question);
        if (error != null) {
            return ResponseEntity.status(BAD_REQUEST).body(error.message());
        }
        var translated = questionService.createTranslated(question);
        var json = getJsonQuestion(translated, true, true, false, true);
        return ResponseEntity.status(OK).body(GSON.toJson(json));
    }

    JsonObject getJsonQuestion(Question question, boolean addDetails, boolean addEntityIds, boolean addTranslatable, boolean addTranslates)
    {
        JsonObject json = new JsonObject();
        Optional.ofNullable(question.getId()).ifPresent(id -> json.addProperty("id", question.getId()));
        for (EntityField entityField : questionService.getQuestionFields().getFields()) {
            entityField.writeJson(question, json);
            if (!addDetails && "answer".equals(entityField.getField()))
                return json;
        }
        if (addEntityIds){
            json.addProperty("topicId", question.getTopic().getId());
            json.addProperty("requirementId", question.getRequirement().getId());
        }
        json.addProperty("tagsHtml", String.join(", ", question.getTags(true)));
        var checkCanTranslate = questionService.checkCanTranslate(question);
        if (checkCanTranslate == null) { // Ok to translate
            if (addTranslatable) {
                json.addProperty("translatable", true);
            }
        }
        else if (checkCanTranslate.questionIdTranslated() != null) { // Add translated question-id
            json.addProperty("translated", checkCanTranslate.questionIdTranslated());
        }
        if (addTranslates) {
            addOriginalForTranslated(question, json);
        }
        return json;
    }

    private void addOriginalForTranslated(Question question, JsonObject json)
    {
        Integer translates = question.getTranslates();
        if (translates != null && examRepositories.getQuestionRepository().existsById(translates)) {
            var original = examRepositories.getQuestionRepository().getReferenceById(translates);
            json.addProperty("question_original", original.getQuestion());
            json.addProperty("answerA_original", original.getAnswerA());
            json.addProperty("answerB_original", original.getAnswerB());
            json.addProperty("answerC_original", original.getAnswerC());
            json.addProperty("answerD_original", original.getAnswerD());
        }
    }

    @PostMapping(path = "questions", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createQuestion(@RequestBody String body)
    {
        JsonObject form = GSON.fromJson(body, JsonObject.class);
        var result = questionService.createQuestion(form);
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
