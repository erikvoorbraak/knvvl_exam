package org.knvvl.exam.services;

import static java.util.function.Predicate.not;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.knvvl.exam.entities.Question.DEFAULT_CERTIFICATE;
import static org.knvvl.exam.services.ExamRepositories.SORT_BY_ID;
import static org.knvvl.exam.services.ExamService.MAX_FILTERS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.knvvl.exam.values.Languages.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class ExamCreationService
{
    private static final Random RANDOM = new Random();
    private static final Predicate<Question> isPracticeQuestion = q -> q.getId() % 2 == 0;

    @Autowired
    ExamService examService;
    @Autowired
    TopicRepository topicRepository;
    @Autowired
    QuestionRepository questionRepository;

    @Transactional
    public void createExam(String label, int certificate, Language language)
    {
        List<Question> questions = new ExamCreator(certificate, language.id()).generateQuestions();
        Exam exam = new Exam(label, certificate, language);
        examService.addExam(exam, questions);
    }

    public Question getPracticeQuestion(int id)
    {
        Question question = questionRepository.getReferenceById(id);
        isTrue(isPracticeQuestion.test(question), "Question is not a practice question: " + id);
        return question;
    }

    public List<Question> createPracticeExam(int topic, int nQuestions, Language language)
    {
        ExamCreator examCreator = new ExamCreator(DEFAULT_CERTIFICATE, language.id());
        examCreator.numQuestions = t -> nQuestions;
        examCreator.topicFilter = t -> t.getId() == topic;
        examCreator.questionFilter = isPracticeQuestion; // Even questions only
        return examCreator.generateQuestions();
    }

    private class ExamCreator
    {
        final int certificate;
        final String language;
        Predicate<Topic> topicFilter = t -> true;
        Predicate<Question> questionFilter = q -> true;
        Function<Topic, Integer> numQuestions = Topic::getNumQuestions;

        private ExamCreator(int certificate, String language)
        {
            this.certificate = certificate;
            this.language = language;
        }

        List<Question> generateQuestions()
        {
            return topicRepository.findAll(SORT_BY_ID).stream()
                .filter(topicFilter)
                .map(topic -> generateForTopic(topic))
                .flatMap(Collection::stream)
                .toList();
        }

        private List<Question> generateForTopic(Topic topic)
        {
            int howManyFilters = MAX_FILTERS;
            var candidatesForTopic = filterForExam(questionRepository.findAll(), language, certificate, topic).filter(questionFilter).toList();
            while (howManyFilters > 0) {
                var examQuestionsForTopic = tryQuestionsForTopic(topic, candidatesForTopic, howManyFilters);
                if (examQuestionsForTopic != null) {
                    return examQuestionsForTopic;
                }
                howManyFilters--;
            }
            throw new IllegalStateException("Not enough questions for topic " + topic + ", certificate " + certificate);
        }

        @Nullable
        private List<Question> tryQuestionsForTopic(Topic topic, List<Question> candidatesForTopic, int howManyFilters)
        {
            List<Question> remainingForTopic = new ArrayList<>(candidatesForTopic);
            List<Question> examQuestionsForTopic = new ArrayList<>();
            int n = numQuestions.apply(topic);
            for (int i = 0; i < n; i++) {
                if (remainingForTopic.isEmpty()) {
                    return null;
                }
                var newQuestion = remainingForTopic.remove(RANDOM.nextInt(remainingForTopic.size()));
                examQuestionsForTopic.add(newQuestion);
                ExamService.removeSimilarQuestions(newQuestion, remainingForTopic, howManyFilters);
            }
            return examQuestionsForTopic;
        }
    }

    public static Stream<Question> filterForExam(List<Question> questions, String language, int certificate, Topic topic)
    {
        return questions.stream()
            .filter(not(Question::isIgnore))
            .filter(topic::hasQuestion)
            .filter(q -> q.getLanguage().equals(language))
            .filter(q -> q.allowForCertificate(certificate));
    }
}
