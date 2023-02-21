package org.knvvl.exam.services;

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
            topicRepository.findAll(SORT_BY_ID).forEach(this::generateForTopic);
            return examQuestions;
        }

        private void generateForTopic(Topic topic)
        {
            topicQuestions.clear();
            allQuestions.stream()
                .filter(q -> topic.equals(q.getTopic()))
                .filter(q -> q.allowForCertificate(certificate))
                .forEach(topicQuestions::add);

            for (int i = 0; i < topic.getNumQuestions(); i++)
            {
                if (topicQuestions.isEmpty())
                {
                    throw new IllegalStateException("Not enough questions for topic " + topic.getLabel() + ", certificate " + certificate);
                }
                addQuestionForTopic();
            }
        }

        private void addQuestionForTopic()
        {
            Question newQuestion = topicQuestions.remove(RANDOM.nextInt(topicQuestions.size()));
            examQuestions.add(newQuestion);
            examService.removeSimilarQuestions(newQuestion, topicQuestions);
        }
    }
}
