package org.knvvl.exam.services;

import static java.util.Arrays.stream;

import static org.apache.commons.lang3.Validate.isTrue;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.util.Strings;
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
    public String importFromJson(Path file) throws IOException
    {
        String json = Files.readString(file);
        Map<Integer, ExamQuestions> cachedQuestions = new HashMap<>();
        Type listType = new TypeToken<ArrayList<ExamAnswerJson>>(){}.getType();
        List<ExamAnswerJson> examAnswers = GSON.fromJson(json, listType);
        var toInsert = examAnswers.stream().map(examAnswer -> toExamAnswer(examAnswer, cachedQuestions)).toList();
        examAnswerRepository.saveAll(toInsert);
        return "Imported " + toInsert.size() + " exam answers";
    }

    @Transactional
    public String importFromTabDelimited(Path file) throws IOException
    {
        String text = Files.readString(file);
        TextReader textReader = new TextReader();
        stream(text.split("\n"))
            .filter(line -> !Strings.isBlank(line))
            .map(String::trim)
            .forEach(textReader::readLine);
        examAnswerRepository.saveAll(textReader.givenAnswers);
        return "Imported " + textReader.givenAnswers.size() + " exam answers";
    }

    private ExamAnswer toExamAnswer(ExamAnswerJson jsonAnswer, Map<Integer, ExamQuestions> cachedQuestions)
    {
        ExamQuestions examQuestions = cachedQuestions.computeIfAbsent(jsonAnswer.exam, examId -> examService.getExamQuestions(examId));
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
        int question; // oneBasedLocalQuestionId
        int topic;
        String answerCorrect;
        String answerGiven;
    }

    private enum TextReaderPhase {NONE, EXAM_ID, CORRECT_ANSWERS, GIVEN_ANSWERS}

    private class TextReader
    {
        TextReaderPhase phase = TextReaderPhase.NONE;
        int examId = 0;
        Map<Integer, List<String>> correctAnswers = new HashMap<>();
        List<ExamAnswer> givenAnswers = new ArrayList<>();
        Map<Integer, ExamQuestions> cachedQuestions = new HashMap<>();

        public void readLine(String line)
        {
            if ("ExamId".equals(line)){
                phase = TextReaderPhase.EXAM_ID;
                correctAnswers.clear();
                return;
            }
            if ("CorrectAnswers".equals(line)){
                phase = TextReaderPhase.CORRECT_ANSWERS;
                return;
            }
            if ("GivenAnswers".equals(line)){
                phase = TextReaderPhase.GIVEN_ANSWERS;
                return;
            }
            isTrue(phase != TextReaderPhase.NONE, "Cannot read data without header, start with 'ExamId'");
            if (phase == TextReaderPhase.EXAM_ID){
                examId = Integer.parseInt(line);
                return;
            }
            if (phase == TextReaderPhase.CORRECT_ANSWERS){
                var cells = line.split("\t");
                int topic = Integer.parseInt(cells[0].trim());
                var answers = stream(cells).skip(1).map(String::trim).toList();
                correctAnswers.put(topic, answers);
                return;
            }
            if (phase == TextReaderPhase.GIVEN_ANSWERS){
                isTrue(examId > 0, "Cannot read answers before ExamId is read");
                isTrue(!correctAnswers.isEmpty(), "Cannot read answers before CorrectAnswers are read");
                readGivenAnswers(line);
            }
        }

        private void readGivenAnswers(String line)
        {
            var cells = line.split("\t");
            String studentId = cells[0].trim();
            int topic = Integer.parseInt(cells[1].trim());
            var answers = stream(cells).skip(2).map(String::trim).toList();
            for (int questionNr = 0; questionNr < answers.size(); questionNr++) {
                int oneBasedLocalQuestionId = questionNr + 1;
                String correctAnswers = getCorrectAnswers(studentId, topic, questionNr);
                String givenAnswer = answers.get(questionNr);
                ExamQuestions examQuestions = cachedQuestions.computeIfAbsent(examId, examId -> examService.getExamQuestions(examId));
                var questionId = examQuestions.get(topic, oneBasedLocalQuestionId).getQuestion().getId();
                givenAnswers.add(new ExamAnswer(studentId, examId, questionId, topic, correctAnswers, givenAnswer));
            }
        }

        @Nonnull
        private String getCorrectAnswers(String studentId, int topic, int questionIndex) {
            List<String> correctForTopic = correctAnswers.get(topic);
            if (correctForTopic == null) {
                throw new IllegalArgumentException("Cannot find correct answer for student " + studentId + ", topic " + topic + ", question " + (questionIndex + 1));
            }
            if (correctForTopic.size() <= questionIndex) {
                throw new IllegalArgumentException("Too few correct answers for student " + studentId + ", topic " + topic + ", question " + (questionIndex + 1));
            }
            return correctForTopic.get(questionIndex);
        }
    }
}
