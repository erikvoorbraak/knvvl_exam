package org.knvvl.exam.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.knvvl.exam.entities.Change;
import org.knvvl.exam.entities.Change.ChangedByAt;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.meta.EntityField;
import org.knvvl.exam.meta.EntityFields;
import org.knvvl.exam.repos.ChangeRepository;
import org.knvvl.exam.repos.PictureRepository;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.RequirementRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;

@Service
public class QuestionService
{
    @Autowired
    private ExamRepositories examRepositories;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ExamService examService;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private RequirementRepository requirementRepository;
    @Autowired
    private PictureRepository pictureRepository;
    @Autowired
    private ChangeRepository changeRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ChangeDetector changeDetector;

    private EntityFields<Question> questionFields;

    public Stream<Question> queryQuestions(Sort sort, int topicId, int requirementId, int examId, String search)
    {
        List<Question> questions;
        if (examId != 0)
            questions = examService.getQuestionsForExam(examId).stream().map(ExamQuestion::getQuestion).toList();
        else
            questions = questionRepository.findAll(sort);

        String searchLower = search.toLowerCase();
        return questions.stream()
            .filter(q -> topicId == 0 || q.getTopic().getId().equals(topicId))
            .filter(q -> requirementId == 0 || q.getRequirement().getId().equals(requirementId))
            .filter(q -> q.applySearch(searchLower));
    }

    private int getNewQuestionId()
    {
        Question question = questionRepository.findTopByOrderByIdDesc();
        return question == null ? 1 : question.getId() + 1;
    }

    public record QuestionCreateResult(@Nullable Question question, @Nullable String message) {}

    @Nonnull
    @Transactional
    public QuestionCreateResult createQuestion(JsonObject form)
    {
        var changedByAt = new ChangedByAt(userService.getCurrentUser(), Instant.now());
        List<Change> changes = new ArrayList<>();
        Question question = new Question();
        question.setId(getNewQuestionId());
        for (EntityField<Question> questionField : getQuestionFields().getFields())
        {
            JsonElement jsonElement = form.get(questionField.getValueField());
            if (jsonElement == null && questionField.isMandatory())
            {
                return new QuestionCreateResult(null, "Mandatory field: " + questionField);
            }
            if (jsonElement != null)
            {
                logChange(changedByAt, question, questionField, changes, () ->
                    questionField.readJson(question, jsonElement));
            }
        }
        questionRepository.save(question);
        changeRepository.saveAll(changes);
        changeDetector.changed();
        return new QuestionCreateResult(question, null);
    }

    @Transactional
    public String updateQuestion(int questionId, JsonObject form)
    {
        var changedByAt = new ChangedByAt(userService.getCurrentUser(), Instant.now());
        List<Change> changes = new ArrayList<>();
        Question question = questionRepository.getReferenceById(questionId);
        for (EntityField<Question> entityField : getQuestionFields().getFields())
        {
            JsonElement jsonElement = form.get(entityField.getValueField());
            if (jsonElement != null)
            {
                logChange(changedByAt, question, entityField, changes, () ->
                    entityField.readJson(question, jsonElement));
            }
        }
        questionRepository.save(question);
        changeRepository.saveAll(changes);
        changeDetector.changed();
        return null;
    }

    private void logChange(ChangedByAt changedByAt, Question question, EntityField<Question> entityField, List<Change> changes, Runnable action)
    {
        String oldValue = entityField.toStringValue(question);
        action.run();
        String newValue = entityField.toStringValue(question);
        if (!Objects.equals(oldValue, newValue))
            changes.add(new Change(changedByAt, question, entityField.getField(), oldValue, newValue));
    }

    public List<Change> getChanges(int questionId)
    {
        return changeRepository.findByChangeKeyQuestionIdOrderByChangeKeyChangedAtDesc(questionId);
    }

    public EntityFields<Question> getQuestionFields()
    {
        if (questionFields == null)
        {
            questionFields = Question.getFields(topicRepository, requirementRepository, pictureRepository);
        }
        return questionFields;
    }
}
