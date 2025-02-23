package org.knvvl.exam.values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knvvl.exam.entities.ExamAnswer;

public class StudentAnswers
{
    private final String studentId;
    private final List<ExamAnswer> examAnswers = new ArrayList<>();

    public StudentAnswers(String studentId)
    {
        this.studentId = studentId;
    }

    public void add(ExamAnswer examAnswer)
    {
        examAnswers.add(examAnswer);
    }

    public boolean hasPassedExam(PassCriteria passCriteria)
    {
        Map<Integer, List<ExamAnswer>> perTopic = new HashMap<>();
        examAnswers.forEach(examAnswer -> perTopic.computeIfAbsent(examAnswer.getTopic(), k -> new ArrayList<>()).add(examAnswer));
        boolean passed = perTopic.size() >= passCriteria.numTopics() &&
            passedForRequirement(passCriteria.thresholdOverall(), examAnswers) &&
            perTopic.values().stream().allMatch(forTopic ->
                passedForRequirement(passCriteria.thresholdPerTopic(), forTopic));
        // For debugging
        //System.out.println(studentId + ": " + passed);
        return passed;
    }

    private static boolean passedForRequirement(int requiredPercentage, List<ExamAnswer> forTopic)
    {
        long total = forTopic.size();
        long correct = forTopic.stream().filter(ExamAnswer::isCorrect).count();
        long percentageCorrect = correct * 100 / total;
        return percentageCorrect >= requiredPercentage;
    }
}
