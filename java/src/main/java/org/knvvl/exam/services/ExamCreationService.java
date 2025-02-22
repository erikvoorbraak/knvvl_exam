package org.knvvl.exam.services;

import static java.util.function.Predicate.not;

import static org.knvvl.exam.services.ExamRepositories.SORT_BY_ID;
import static org.knvvl.exam.services.ExamService.MAX_FILTERS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
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

    @Autowired
    ExamService examService;
    @Autowired
    TopicRepository topicRepository;
    @Autowired
    QuestionRepository questionRepository;

    @Transactional
    public void createExam(String label, int certificate, Language language)
    {
        Exam exam = new Exam(label, certificate, language);
        List<Question> questions = topicRepository.findAll(SORT_BY_ID).stream()
            .map(topic -> generateForTopic(exam, topic))
            .flatMap(Collection::stream)
            .toList();
        examService.addExam(exam, questions);
    }

    public static Stream<Question> filterForExam(List<Question> questions, Exam exam, Topic topic)
    {
        return questions.stream()
            .filter(not(Question::isIgnore))
            .filter(topic::hasQuestion)
            .filter(q -> q.getLanguage().equals(exam.getLanguage()))
            .filter(q -> q.allowForCertificate(exam.getCertificate()));
    }

    private List<Question> generateForTopic(Exam exam, Topic topic)
    {
        int howManyFilters = MAX_FILTERS;
        var candidatesForTopic = filterForExam(questionRepository.findAll(), exam, topic).toList();
        while (howManyFilters > 0) {
            var examQuestionsForTopic = tryQuestionsForTopic(topic, candidatesForTopic, howManyFilters);
            if (examQuestionsForTopic != null) {
                return examQuestionsForTopic;
            }
            howManyFilters--;
        }
        throw new IllegalStateException("Not enough questions for topic " + topic + ", certificate " + exam.getCertificate());
    }

    @Nullable
    private static List<Question> tryQuestionsForTopic(Topic topic, List<Question> candidatesForTopic, int howManyFilters)
    {
        List<Question> remainingForTopic = new ArrayList<>(candidatesForTopic);
        List<Question> examQuestionsForTopic = new ArrayList<>();
        for (int i = 0; i < topic.getNumQuestions(); i++) {
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
