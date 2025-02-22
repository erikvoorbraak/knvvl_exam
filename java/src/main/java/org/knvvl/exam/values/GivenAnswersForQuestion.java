package org.knvvl.exam.values;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knvvl.exam.entities.ExamAnswer;

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

    public String toString()
    {
        return questionId + ": " + examAnswers.size();
    }
}
