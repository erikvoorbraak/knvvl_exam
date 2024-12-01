package org.knvvl.exam.rest;

import static java.util.function.Predicate.not;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.services.RequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("api")
public class RequirementRestService
{
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    private RequirementService requirementService;

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping(value = "/requirements", produces = APPLICATION_JSON_VALUE)
    String getRequirements()
    {
        List<Question> questions = questionRepository.findAll();
        JsonArray all = new JsonArray();
        for (Requirement requirement : requirementService.getAll())
        {
            JsonObject json = getJson(requirement);
            json.addProperty("nQuestions", activeQuestions(questions, q -> requirement.equals(q.getRequirement())));
            all.add(json);
        }
        return GSON.toJson(all);
    }

    @Nonnull
    private static JsonObject getJson(Requirement requirement)
    {
        JsonObject json = new JsonObject();
        json.addProperty("id", requirement.getId());
        json.addProperty("label", requirement.getLabel());
        json.addProperty("topic", requirement.getTopicLabel());
        json.addProperty("topicId", requirement.getTopicId());
        json.addProperty("domain", requirement.getDomain());
        json.addProperty("domainTitle", requirement.getDomainTitle());
        json.addProperty("subdomain", requirement.getSubdomain());
        return json;
    }

    @GetMapping(value = "/requirements/{requirementId}", produces = APPLICATION_JSON_VALUE)
    String getQuestion(@PathVariable("requirementId") int requirementId)
    {
        var requirement = requirementService.getById(requirementId);
        var json = getJson(requirement);
        return GSON.toJson(json);
    }

    private static long activeQuestions(List<Question> questions, Predicate<Question> tester)
    {
        return questions.stream().filter(not(Question::isIgnore)).filter(tester).count();
    }

    @PostMapping(path = "requirements", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createRequirement(@RequestBody String body)
    {
        JsonObject form = GSON.fromJson(body, JsonObject.class);
        var result = requirementService.createRequirement(form);
        if (result.message() != null)
            return ResponseEntity.status(BAD_REQUEST).body(result.message());
        return ResponseEntity.status(OK).body(null);
    }

    @PostMapping(path = "requirements/{requirementId}", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateQuestion(@PathVariable("requirementId") int requirementId, @RequestBody String body)
    {
        JsonObject form = GSON.fromJson(body, JsonObject.class);
        String message = requirementService.updateRequirement(requirementId, form);
        if (message != null)
            return ResponseEntity.status(BAD_REQUEST).body(message);
        return ResponseEntity.status(OK).body(null);
    }

    @DeleteMapping(path = "requirements/{requirementId}")
    private void deleteExam(@PathVariable("requirementId") int requirementId)
    {
        requirementService.deleteRequirement(requirementId);
    }
}