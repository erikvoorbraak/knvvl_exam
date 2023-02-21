package org.knvvl.exam.services;

import java.util.ArrayList;
import java.util.List;

import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Picture;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Requirement;
import org.knvvl.exam.entities.Topic;
import org.knvvl.exam.repos.ExamQuestionRepository;
import org.knvvl.exam.repos.ExamRepository;
import org.knvvl.exam.repos.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class ExamService
{
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private ExamQuestionRepository examQuestionRepository;
    @Autowired
    private QuestionRepository questionRepository;

    @Transactional
    public void addExam(Exam exam, List<Question> questions)
    {
        if (exam.getId() == null)
            exam.setId(examRepository.findTopByOrderByIdDesc().getId() + 1);
        examRepository.save(exam);
        Integer examId = exam.getId();
        List<ExamQuestion> examQuestions = new ArrayList<>(questions.size());
        for (int i = 0; i < questions.size(); i++)
        {
            Question question = questions.get(i);
            examQuestions.add(new ExamQuestion(examId, question, question.getTopic(), i));
        }
        examQuestionRepository.saveAll(examQuestions);
    }

    /**
     *
     * @param examId
     * @return Questions ordered by how they appeared in the exam
     */
    public List<ExamQuestion> getQuestionsForExam(int examId)
    {
        return examQuestionRepository.findByExamOrderByQuestionIndex(examId);
    }

    @Transactional
    public void setAltQuestion(ExamQuestion examQuestion, int altQuestionId)
    {
        examQuestion.setQuestion(questionRepository.getReferenceById(altQuestionId));
        examQuestionRepository.save(examQuestion);
    }

    public List<Question> getAltQuestions(ExamQuestion examQuestion)
    {
        Question questionToGetAlt = examQuestion.getQuestion();
        Topic topic = questionToGetAlt.getTopic();
        // Start with all questions of this topic
        List<Question> otherQuestionsForTopic = new ArrayList<>(questionRepository.findByTopicOrderById(topic));
        // Remove questions having same picture, group, etc
        removeSimilarQuestions(questionToGetAlt, otherQuestionsForTopic);
        // Remove questions that are already in this exam
        getQuestionsForExam(examQuestion.getExam()).forEach(eq -> otherQuestionsForTopic.remove(eq.getQuestion())); // includes questionToGetAlt

        // Set order: first questions with same requirement, then with same topic
        List<Question> altQuestions = new ArrayList<>();
        altQuestions.add(questionToGetAlt);
        Requirement requirement = questionToGetAlt.getRequirement();
        otherQuestionsForTopic.stream().filter(q -> q.getRequirement().equals(requirement)).forEach(altQuestions::add);
        otherQuestionsForTopic.stream().filter(q -> !q.getRequirement().equals(requirement)).forEach(altQuestions::add);
        return altQuestions;
    }

    @Transactional
    public void deleteExam(int examId)
    {
        List<ExamQuestion> questions = examQuestionRepository.findByExamOrderByQuestionIndex(examId);
        examQuestionRepository.deleteAll(questions);
        examRepository.delete(examRepository.getReferenceById(examId));
    }

    public void removeSimilarQuestions(Question examQuestion, List<Question> removeFrom)
    {
        // Use any given picture only once
        Picture pictureInExam = examQuestion.getPicture();
        if (pictureInExam != null)
            removeFrom.removeIf(q -> pictureInExam.equals(q.getPicture()));

        // Include at most one question in a given examGroup
        String examGroup = examQuestion.getExamGroup();
        if (!examGroup.isEmpty())
            removeFrom.removeIf(q -> examGroup.equals(q.getExamGroup()));
    }
}
