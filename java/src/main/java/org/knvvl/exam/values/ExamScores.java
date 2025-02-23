package org.knvvl.exam.values;

import static org.knvvl.exam.values.GivenAnswersForQuestion.EMPTY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.knvvl.exam.entities.ExamAnswer;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Question;
import org.knvvl.exam.repos.ExamAnswerRepository;

public class ExamScores
{
    private final ExamAnswerRepository examAnswerRepository;
    private final List<ExamAnswer> examAnswers = new ArrayList<>();
    private final Map<Integer, GivenAnswersForQuestion> answersPerQuestion = new HashMap<>();

    public ExamScores(ExamAnswerRepository examAnswerRepository)
    {
        this.examAnswerRepository = examAnswerRepository;
    }

    public ExamScores addForExam(int examId)
    {
        addAll(examAnswerRepository.findByExamAnswerKeyExam(examId));
        return this;
    }

    public ExamScores addHistoricScores(List<ExamQuestion> questionsInExam)
    {
        List<Integer> questionIds = questionsInExam.stream()
            .map(ExamQuestion::getQuestion)
            .map(Question::getId)
            .toList();
        addAll(examAnswerRepository.findByExamAnswerKeyQuestionIn(questionIds));
        return this;
    }

    public ExamScores addForQuestion(int questionId)
    {
        addAll(examAnswerRepository.findByExamAnswerKeyQuestion(questionId));
        return this;
    }

    private void addAll(List<ExamAnswer> examAnswersToAdd)
    {
        examAnswers.addAll(examAnswersToAdd);
        examAnswersToAdd.forEach(examAnswer ->
            answersPerQuestion.computeIfAbsent(examAnswer.getQuestion(), GivenAnswersForQuestion::new).add(examAnswer));
    }

    public double getScorePercentage()
    {
        long numCorrect = examAnswers.stream().filter(ExamAnswer::isCorrect).count();
        return toPromille(numCorrect, examAnswers.size());
    }

    private static double toPromille(long numCorrect, int numTotal)
    {
        return numTotal == 0 ? 0 : Math.round(numCorrect * 1000.0 / numTotal) / 10.0d;
    }

    @Nonnull
    public GivenAnswersForQuestion getForQuestion(int questionId)
    {
        return Objects.requireNonNullElse(answersPerQuestion.get(questionId), EMPTY);
    }

    public double getPercentagePassed(PassCriteria passCriteria)
    {
        List<StudentAnswers> studentAnswers = getStudentAnswers();
        long numPassed = studentAnswers.stream()
            .filter(forStudent -> forStudent.hasPassedExam(passCriteria))
            .count();
        return toPromille(numPassed, studentAnswers.size());
    }

    private List<StudentAnswers> getStudentAnswers()
    {
        Map<String, StudentAnswers> perStudent = new HashMap<>();
        examAnswers.forEach(examAnswer -> perStudent.computeIfAbsent(examAnswer.getStudent(), StudentAnswers::new).add(examAnswer));
        return new ArrayList<>(perStudent.values());
    }
}
