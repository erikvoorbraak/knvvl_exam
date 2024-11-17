package org.knvvl.exam.services;

import static java.util.function.Predicate.not;

import static org.knvvl.exam.services.ExamRepositories.SORT_BY_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.TopicRepository;
import org.knvvl.exam.services.Languages.Language;
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
        List<Question> questions = new ExamCreationContext(exam).generate();
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

    private class ExamCreationContext
    {
        final Exam exam;
        final List<Question> allQuestions = questionRepository.findAll();
        final List<Question> examQuestions = new ArrayList<>();
        final List<Question> topicQuestions = new ArrayList<>();

        private ExamCreationContext(Exam exam)
        {
            this.exam = exam;
        }

        List<Question> generate()
        {
            topicRepository.findAll(SORT_BY_ID).forEach(this::addQuestionsForTopic);
            return examQuestions;
        }

        private void addQuestionsForTopic(Topic topic)
        {
            topicQuestions.clear();
            filterForExam(allQuestions, exam, topic).forEach(topicQuestions::add);

            for (int i = 0; i < topic.getNumQuestions(); i++)
            {
                addQuestionForTopic(topic);
            }
        }

        private void addQuestionForTopic(Topic topic)
        {
            if (topicQuestions.isEmpty())
            {
                throw new IllegalStateException("Not enough questions for topic " + topic + ", certificate " + exam.getCertificate());
            }
            Question newQuestion = topicQuestions.remove(RANDOM.nextInt(topicQuestions.size()));
            examQuestions.add(newQuestion);
            examService.removeSimilarQuestions(newQuestion, topicQuestions);
        }
    }
}
