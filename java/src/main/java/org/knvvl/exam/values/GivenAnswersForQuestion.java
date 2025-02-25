package org.knvvl.exam.values;

import static java.util.stream.Collectors.joining;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.knvvl.exam.entities.ExamAnswer;
import org.knvvl.exam.repos.ExamRepository;

public class GivenAnswersForQuestion
{
    public static final GivenAnswersForQuestion EMPTY = new GivenAnswersForQuestion(0);

    private final int questionId;
    private final List<ExamAnswer> examAnswers = new ArrayList<>();

    GivenAnswersForQuestion(int questionId)
    {
        this.questionId = questionId;
    }

    void add(ExamAnswer examAnswer)
    {
        isTrue(questionId != 0);
        examAnswers.add(examAnswer);
    }

    public List<ExamAnswer> getExamAnswers()
    {
        return examAnswers;
    }

    public boolean isEmpty()
    {
        return examAnswers.isEmpty();
    }

    public int getNumExams()
    {
        Set<Integer> exams = new HashSet<>();
        examAnswers.forEach(a -> exams.add(a.getExam()));
        return exams.size();
    }

    public int getNumAnswers()
    {
        return examAnswers.size();
    }

    public int getScorePercentage()
    {
        if (examAnswers.isEmpty()) {
            return -1;
        }
        return (int)examAnswers.stream().filter(ExamAnswer::isCorrect).count() * 100 / examAnswers.size();
    }

    public String toStringPerExam(ExamRepository examRepository)
    {
        if (examAnswers.isEmpty()) {
            return "";
        }
        Map<Integer, GivenAnswersForQuestion> perExam = new HashMap<>();
        examAnswers.forEach(examAnswer -> perExam.computeIfAbsent(examAnswer.getExam(), e -> new GivenAnswersForQuestion(questionId)).add(examAnswer));
        String perExamPerc = perExam.entrySet().stream()
            .sorted(Entry.comparingByKey())
            .map(e -> e.getKey() + " " + getExamSafe(e.getKey(), examRepository) +
                ": " + e.getValue().getScorePercentage() + "% (" + e.getValue().getNumAnswers() + " answers)\n")
            .collect(joining(""));
        return perExamPerc + "Overall: " + getScorePercentage() + "% (#=" + getNumAnswers() + ")";
    }

    private String getExamSafe(int examId, ExamRepository examRepository)
    {
        try
        {
            return examRepository.getReferenceById(examId).getLabel();
        }
        catch (RuntimeException e)
        {
            return "";
        }
    }

    public String toString()
    {
        return questionId + ": " + examAnswers.size();
    }
}
