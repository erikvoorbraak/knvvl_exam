package org.knvvl.exam.services;

import static java.util.function.Predicate.not;

import static org.knvvl.exam.services.ExamRepositories.SORT_BY_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.repos.QuestionRepository;
import org.knvvl.exam.repos.TopicRepository;
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
    public void createExam(String label, int certificate)
    {
        Exam exam = new Exam(label, certificate);
        List<Question> questions = new ExamCreationContext(certificate).generate();
        examService.addExam(exam, questions);
    }

    private class ExamCreationContext
    {
        final int certificate;
        final List<Question> allQuestions = questionRepository.findAll();
        final List<Question> examQuestions = new ArrayList<>();
        final List<Question> topicQuestions = new ArrayList<>();

        private ExamCreationContext(int certificate)
        {
            this.certificate = certificate;
        }

        List<Question> generate()
        {
            topicRepository.findAll(SORT_BY_ID).forEach(this::addQuestionsForTopic);
            return examQuestions;
        }

        private void addQuestionsForTopic(Topic topic)
        {
            topicQuestions.clear();
            allQuestions.stream()
                .filter(not(Question::isIgnore))
                .filter(topic::hasQuestion)
                .filter(q -> q.allowForCertificate(certificate))
                .forEach(topicQuestions::add);

            for (int i = 0; i < topic.getNumQuestions(); i++)
            {
                addQuestionForTopic(topic);
            }
        }

        private void addQuestionForTopic(Topic topic)
        {
            if (topicQuestions.isEmpty())
            {
                throw new IllegalStateException("Not enough questions for topic " + topic + ", certificate " + certificate);
            }
            Question newQuestion = topicQuestions.remove(RANDOM.nextInt(topicQuestions.size()));
            examQuestions.add(newQuestion);
            examService.removeSimilarQuestions(newQuestion, topicQuestions);
        }
    }
}
