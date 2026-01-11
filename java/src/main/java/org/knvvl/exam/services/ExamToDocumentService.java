package org.knvvl.exam.services;

import java.util.List;
import java.util.Map;

import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.knvvl.exam.entities.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExamToDocumentService
{
    @Autowired ExamService examService;
    @Autowired TextService textService;

    public byte[] generatePdf(Exam exam, boolean withQuestionId)
    {
        List<ExamQuestion> questions = examService.getExamQuestionsForExam(exam.getId());
        return new ExamGenerator(textService, exam, questions, withQuestionId).generatePdf();
    }

    public String generateHtml(Exam exam)
    {
        List<ExamQuestion> examQuestions = examService.getExamQuestionsForExam(exam.getId());
        List<Question> questions = examQuestions.stream().map(ExamQuestion::getQuestion).toList();
        return generateHtml(questions);
    }

    public String generateHtml(List<Question> questions)
    {
        return new ExamGeneratorHtml().generateHtml(questions);
    }

    public String checkPracticeExam(int topicId, List<Question> questions, Map<String, String> questionsToAnswers)
    {
        return new ExamGeneratorHtml().checkPracticeExam(questions, questionsToAnswers, examService.getPassCriteria());
    }
}
