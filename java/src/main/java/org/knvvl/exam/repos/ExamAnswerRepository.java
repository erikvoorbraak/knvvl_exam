package org.knvvl.exam.repos;

import java.util.Collection;
import java.util.List;

import org.knvvl.exam.entities.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, ExamAnswer.ExamAnswerKey>
{
    List<ExamAnswer> findByExamAnswerKeyQuestion(int questionId);

    List<ExamAnswer> findByExamAnswerKeyQuestionIn(Collection<Integer> questionIds);

    List<ExamAnswer> findByExamAnswerKeyExam(int examId);
}
