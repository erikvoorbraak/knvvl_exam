package org.knvvl.exam.services;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knvvl.exam.entities.ExamAnswer;
import org.knvvl.exam.repos.ExamAnswerRepository;
import org.knvvl.exam.values.ExamQuestions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import jakarta.transaction.Transactional;

@Service
public class ExamAnswersImportService
{
    private static Gson GSON = new Gson();

    @Autowired private ExamService examService;
    @Autowired private ExamAnswerRepository examAnswerRepository;

    @Transactional
    public String importFrom(Path file) throws IOException
    {
        Map<Integer, ExamQuestions> cachedQuestions = new HashMap<>();
        String json = Files.readString(file);
        Type listType = new TypeToken<ArrayList<ExamAnswerJson>>(){}.getType();
        List<ExamAnswerJson> examAnswers = GSON.fromJson(json, listType);
        var toInsert = examAnswers.stream().map(examAnswer -> toExamAnswer(examAnswer, cachedQuestions)).toList();
        examAnswerRepository.saveAll(toInsert);
        return "Imported " + toInsert.size() + " exam answers";
    }

    private ExamAnswer toExamAnswer(ExamAnswerJson jsonAnswer, Map<Integer, ExamQuestions> cachedQuestions)
    {
        var examQuestions = cachedQuestions.computeIfAbsent(jsonAnswer.exam, examId -> examService.getExamQuestions(examId));
        var questionId = examQuestions.get(jsonAnswer.topic, jsonAnswer.question).getQuestion().getId();
        return new ExamAnswer(
            jsonAnswer.student,
            jsonAnswer.exam,
            questionId,
            jsonAnswer.topic,
            jsonAnswer.answerCorrect,
            jsonAnswer.answerGiven);
    }

    private static class ExamAnswerJson
    {
        String student;
        int exam;
        int question;
        int topic;
        String answerCorrect;
        String answerGiven;
    }
}
