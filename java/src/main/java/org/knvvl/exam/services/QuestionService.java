package org.knvvl.exam.services;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import static org.knvvl.exam.meta.Config.EXAM_CHATGPT_APIKEY;
import static org.knvvl.exam.meta.Config.EXAM_CHATGPT_INSTRUCTIONS;
import static org.knvvl.exam.meta.Config.EXAM_CHATGPT_MODEL;
import static org.knvvl.exam.meta.Config.EXAM_TARGET_LANGUAGE;
import static org.knvvl.tools.chatgpt.Message.Role.USER;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.knvvl.exam.values.Languages;
import org.knvvl.exam.values.Languages.Language;
import org.knvvl.exam.values.CreateEntityResult;
import org.knvvl.tools.chatgpt.ChatGptConfig;
import org.knvvl.tools.chatgpt.Message;
import org.knvvl.tools.chatgpt.Model;
import org.knvvl.tools.chatgpt.SimpleChatGptClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;

@Service
public class QuestionService
{
    private static final String CHATGPT_MODEL_ERROR = "Unknown model for ChatGPT, supported models: " +
        stream(Model.values()).map(Model::id).collect(joining(", "));

    @Autowired
    private ExamRepositories examRepositories;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private TextService textService;
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
    /**
     * Given a question, which languages is it translated to?
     */
    private static final Map<Integer, Map<String, Integer>> translationsCache = new HashMap<>();

    public Stream<Question> queryQuestions(Sort sort, String language, int topicId, int requirementId, int examId, String search)
    {
        List<Question> questions;
        if (examId != 0)
            questions = examService.getExamQuestionsForExam(examId).stream().map(ExamQuestion::getQuestion).toList();
        else
            questions = Strings.isNullOrEmpty(language)
                ? questionRepository.findAll(sort)
                : questionRepository.findByLanguage(language, sort);

        questions.forEach(QuestionService::cacheTranslation);

        String searchLower = search.toLowerCase();
        return questions.stream()
            .filter(q -> topicId == 0 || q.getTopic().getId().equals(topicId))
            .filter(q -> requirementId == 0 || q.getRequirement().getId().equals(requirementId))
            .filter(q -> q.applySearch(searchLower));
    }

    private static void cacheTranslation(Question question)
    {
        Integer translates = question.getTranslates();
        if (translates != null)
        {
            getTranslationsCache(translates).put(question.getLanguage(), question.getId());
        }
    }

    private static void uncacheTranslation(Question question)
    {
        Integer translates = question.getTranslates();
        if (translates != null)
        {
            getTranslationsCache(translates).remove(question.getLanguage());
        }
    }

    /**
     * @param questionId To get tranlsations for
     * @return Map of translations for the given question: language -> translation
     */
    private static Map<String, Integer> getTranslationsCache(Integer questionId)
    {
        return translationsCache.computeIfAbsent(questionId, HashMap::new);
    }

    private int getNewQuestionId()
    {
        Question question = questionRepository.findTopByOrderByIdDesc();
        return question == null ? 1 : question.getId() + 1;
    }

    public record QuestionCreateResult(@Nullable Question question, @Nullable String message) {}

    @Nonnull
    @Transactional
    public CreateEntityResult createQuestion(JsonObject form)
    {
        var changedByAt = new ChangedByAt(userService.getCurrentUser());
        List<Change> changes = new ArrayList<>();
        Question question = new Question();
        question.setId(getNewQuestionId());
        for (EntityField<Question> questionField : getQuestionFields().getFields())
        {
            JsonElement jsonElement = form.get(questionField.getValueField());
            if (jsonElement == null && questionField.isMandatory())
            {
                return new CreateEntityResult(null, "Mandatory field: " + questionField);
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
        cacheTranslation(question);
        return new CreateEntityResult(question, null);
    }

    @Transactional
    public String updateQuestion(int questionId, JsonObject form)
    {
        var changedByAt = new ChangedByAt(userService.getCurrentUser());
        List<Change> changes = new ArrayList<>();
        Question question = questionRepository.getReferenceById(questionId);

        uncacheTranslation(question);
        applyAndLogChanges(form, changedByAt, question, changes);
        // Verify that "translates" field points to an existing question
        Integer translates = question.getTranslates();
        if (translates != null && !questionRepository.existsById(translates))
            return "Field 'Translates question' does not refer to an existing question: " + translates;
        cacheTranslation(question);

        questionRepository.save(question);
        changeRepository.saveAll(changes);
        changeDetector.changed();
        return null;
    }

    private void applyAndLogChanges(JsonObject form, ChangedByAt changedByAt, Question question, List<Change> changes)
    {
        for (EntityField<Question> entityField : getQuestionFields().getFields())
        {
            JsonElement jsonElement = form.get(entityField.getValueField());
            if (jsonElement != null)
            {
                logChange(changedByAt, question, entityField, changes, () ->
                    entityField.readJson(question, jsonElement));
            }
        }
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

    /**
     *
     * @param question to check for
     * @return Information why it cannot be translated, or null if it can be translated
     */
    public CheckCanTranslate checkCanTranslate(Question question)
    {
        var targetLanguage = textService.get(EXAM_TARGET_LANGUAGE);
        if (isNullOrEmpty(targetLanguage))
        {
            return new CheckCanTranslate("No language to translate to configured in Settings: '" + EXAM_TARGET_LANGUAGE + "'", null);
        }
        if (targetLanguage.equals(question.getLanguage()))
        {
            return new CheckCanTranslate("This question is already in language " + targetLanguage, null);
        }
        var translationId = getTranslationsCache(question.getId()).get(targetLanguage);
        if (translationId != null)
        {
            return new CheckCanTranslate("Already found translation to " + targetLanguage + " for question " + question.getId(), translationId);
        }
        return null;
    }

    public record CheckCanTranslate(String message, Integer questionIdTranslated) {}

    public Question createTranslated(Question question)
    {
        var targetLanguage = Languages.get(textService.get(EXAM_TARGET_LANGUAGE));
        var translated = new Question();
        getQuestionFields().getFields().forEach(f -> f.copyValue(question, translated));
        translated.setTranslates(question.getId());
        translated.setLanguage(targetLanguage.id());
        translated.setRemarks("");
        translated.setIgnore(true);
        translated.setDiscuss(true);

        getQuestionFields().getFields().stream()
            .filter(f -> Question.TRANSLATABLE_FIELDS.contains(f.getField()))
            .map(EntityField.EntityFieldString.class::cast)
            .forEach(sf -> sf.setStringValue(translated, translate(sf.toStringValue(question), targetLanguage)));
        return translated;
    }

    private String translate(String text, Language targetLanguage)
    {
        var chatGptClient = createChatGptClient();
        if (chatGptClient == null)
            return "";
        var instructionsPattern = textService.get(EXAM_CHATGPT_INSTRUCTIONS);
        if (Strings.isNullOrEmpty(instructionsPattern))
            return "";
        var instructions = instructionsPattern.replace("{0}", targetLanguage.label()).replace("{1}", text);
        var message = new Message(USER, instructions);
        return chatGptClient.sendChatMessages(message);
    }

    @Nullable
    private SimpleChatGptClient createChatGptClient()
    {
        var apiKey = textService.get(EXAM_CHATGPT_APIKEY);
        if (Strings.isNullOrEmpty(apiKey))
            return null;
        var modelName = textService.get(EXAM_CHATGPT_MODEL);
        var model = stream(Model.values()).filter(m -> m.id().equals(modelName))
            .findFirst().orElseThrow(() -> new IllegalArgumentException(CHATGPT_MODEL_ERROR));
        return new SimpleChatGptClient(ChatGptConfig.create().withApiKey(apiKey).withModel(model).build());
    }

    private List<Change> getChangesForTranslated(Question translated)
    {
        List<Change> changes = new ArrayList<>();
        var changedByAt = new ChangedByAt(userService.getCurrentUser());
        for (var field : getQuestionFields().getFields())
        {
            String newValue = field.toStringValue(translated);
            if (!newValue.isEmpty())
                changes.add(new Change(changedByAt, translated, field.getField(), "", newValue));
        }
        return changes;
    }
}
