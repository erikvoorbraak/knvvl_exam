package org.knvvl.exam;

import static java.util.Collections.emptyList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.knvvl.exam.services.Languages.LANGUAGE_NL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knvvl.exam.entities.Change;
import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.repos.ChangeRepository;
import org.knvvl.exam.repos.RequirementRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.knvvl.exam.services.ExamRepositories;
import org.knvvl.exam.services.ExamService;
import org.knvvl.exam.services.Languages;
import org.knvvl.exam.services.QuestionService;
import org.knvvl.exam.services.QuestionService.QuestionCreateResult;
import org.knvvl.exam.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.JsonObject;

import jakarta.transaction.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExamApplication.class)
@Transactional
public class ExamRepositoriesIntegrationTest
{
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private ExamRepositories examRepositories;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private ExamService examService;
    @Autowired
    private ChangeRepository changeRepository;
    @Autowired
    private UserService userService;

    @Before
    public void setUp()
    {
        userService.addUser("theuser", "password", "a@b.c");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("theuser");
        userService.setAuthenticationSupplier(() -> authentication);
    }

    @Test
    public void saveTopic()
    {
        givenTopic();
        examRepositories.addTopic(new Topic("B"));
        assertEquals("With assigned id", "A", topicRepository.getReferenceById(1).getLabel());
        assertEquals("Auto-generated id", "B", topicRepository.getReferenceById(2).getLabel());
    }

    @Test
    public void getExamQuestionsByExamId()
    {
        givenTopic();
        givenRequirement();
        Question question1 = givenQuestion("A");
        Question question2 = givenQuestion("B");

        Exam exam = new Exam("Jan 2023", 2, LANGUAGE_NL);
        exam.setId(1);
        examService.addExam(exam, List.of(question1, question2));
        examService.addExam(new Exam("Feb 2023", 2, LANGUAGE_NL), emptyList());

        assertEquals(2, examRepositories.getExamQuestionRepository().findByExamOrderByQuestionIndex(1).size());
        assertEquals(0, examRepositories.getExamQuestionRepository().findByExamOrderByQuestionIndex(2).size());
    }

    @Test
    public void getChangesForQuestion()
    {
        givenTopic();
        givenRequirement();
        Question question1 = givenQuestion("A");

        List<Change> changes = changeRepository.findByChangeKeyQuestionIdOrderByChangeKeyChangedAtDesc(question1.getId());
        assertNotNull(changes);
    }

    private Question givenQuestion(String s)
    {
        JsonObject json = new JsonObject();
        json.addProperty("topicId", 1);
        json.addProperty("requirementId", 1);
        json.addProperty("question", s);
        json.addProperty("answerA", s);
        json.addProperty("answerB", s);
        json.addProperty("answerC", s);
        json.addProperty("answerD", s);
        json.addProperty("answer", "A");
        QuestionCreateResult result = questionService.createQuestion(json);
        assertNotNull(result.question());
        assertNull(result.message());
        return result.question();
    }

    private Topic givenTopic()
    {
        Topic topic = new Topic("A");
        topic.setId(1);
        topicRepository.save(topic);
        return topic;
    }

    private Requirement givenRequirement()
    {
        Requirement requirement = new Requirement();
        requirement.setId(1);
        requirement.setLabel("A");
        requirementRepository.save(requirement);
        return requirement;
    }
}