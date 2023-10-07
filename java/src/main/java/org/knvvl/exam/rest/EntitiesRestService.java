package org.knvvl.exam.rest;

import static java.util.function.Predicate.not;

import static org.knvvl.exam.rest.QuestionRestService.GSON;
import static org.knvvl.exam.services.ExamRepositories.SORT_BY_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.util.List;
import java.util.function.Predicate;

import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.entities.Text;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.RequirementRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.knvvl.exam.services.TextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("api")
public class EntitiesRestService
{
    @Autowired
    private TextService textService;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping(value = "/texts", produces = APPLICATION_JSON_VALUE)
    String getTexts()
    {
        JsonArray all = new JsonArray();
        for (Text text : textService.findAll())
        {
            JsonObject json = new JsonObject();
            json.addProperty("key", text.getKey());
            json.addProperty("label", text.getLabel());
            all.add(json);
        }
        return GSON.toJson(all);
    }

    @GetMapping(value = "/texts/{textKey}", produces = TEXT_PLAIN_VALUE)
    String getText(@PathVariable("textKey") String textKey)
    {
        return textService.getReferenceById(textKey).getLabel();
    }

    @PostMapping(value = "/texts/{textKey}", consumes = TEXT_PLAIN_VALUE)
    void saveText(@PathVariable("textKey") String textKey, @RequestBody String body)
    {
        textService.save(textKey, body);
    }

    @GetMapping(value = "/topics", produces = APPLICATION_JSON_VALUE)
    String getTopics()
    {
        List<Question> questions = questionRepository.findAll();
        JsonArray all = new JsonArray();
        for (Topic topic : topicRepository.findAll(SORT_BY_ID))
        {
            JsonObject json = new JsonObject();
            json.addProperty("id", topic.getId());
            json.addProperty("label", topic.getLabel());
            json.addProperty("numQuestions", topic.getNumQuestions());
            json.addProperty("nQuestions", activeQuestions(questions, q -> topic.equals(q.getTopic())));
            all.add(json);
        }
        return GSON.toJson(all);
    }

    @GetMapping(value = "/requirements", produces = APPLICATION_JSON_VALUE)
    String getRequirements()
    {
        List<Question> questions = questionRepository.findAll();
        JsonArray all = new JsonArray();
        for (Requirement requirement : requirementRepository.findAll(SORT_BY_ID))
        {
            JsonObject json = new JsonObject();
            json.addProperty("id", requirement.getId());
            json.addProperty("label", requirement.getLabel());
            json.addProperty("topic", requirement.getTopic().getLabel());
            json.addProperty("domain", requirement.getDomain());
            json.addProperty("domainTitle", requirement.getDomainTitle());
            json.addProperty("subdomain", requirement.getSubdomain());
            json.addProperty("nQuestions", activeQuestions(questions, q -> requirement.equals(q.getRequirement())));
            all.add(json);
        }
        return GSON.toJson(all);
    }

    private static long activeQuestions(List<Question> questions, Predicate<Question> tester)
    {
        return questions.stream().filter(not(Question::isIgnore)).filter(tester).count();
    }

}
