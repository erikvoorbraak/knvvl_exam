package org.knvvl.exam.services;

import java.util.List;

import org.knvvl.exam.entities.Exam;
import org.knvvl.exam.entities.ExamQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExamToPdfService
{
    @Autowired ExamService examService;
    @Autowired TextService textService;

    public byte[] generatePdf(Exam exam)
    {
        List<ExamQuestion> questions = examService.getQuestionsForExam(exam.getId());
        return new ExamGenerator(textService, exam, questions).generatePdf();
    }
}
