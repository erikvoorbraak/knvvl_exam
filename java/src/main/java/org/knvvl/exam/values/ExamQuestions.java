package org.knvvl.exam.values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.knvvl.exam.entities.ExamQuestion;

public class ExamQuestions
{
    private final int examId;
    private final Map<Integer, List<ExamQuestion>> examQuestions = new HashMap<>();

    public ExamQuestions(int examId, List<ExamQuestion> orderedExamQuestions)
    {
        this.examId = examId;
        orderedExamQuestions.forEach(examQuestion ->
            examQuestions.computeIfAbsent(examQuestion.getTopic().getId(), tid -> new ArrayList<>()).add(examQuestion));
    }

    @Nonnull
    public ExamQuestion get(int topicId, int oneBasedLocalQuestionId)
    {
        List<ExamQuestion> forTopic = examQuestions.get(topicId);
        if (forTopic == null) {
            throw new IllegalArgumentException("No exam questions for topic " + topicId);
        }
        ExamQuestion forQuestion = forTopic.get(oneBasedLocalQuestionId - 1);
        if (forQuestion == null) {
            throw new IllegalArgumentException("No exam questions for topic " + topicId + " and question " + oneBasedLocalQuestionId);
        }
        return forQuestion;
    }
}
