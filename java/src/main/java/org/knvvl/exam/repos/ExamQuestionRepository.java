package org.knvvl.exam.repos;

import java.util.List;

import org.knvvl.exam.entities.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Integer>
{
    List<ExamQuestion> findByExamOrderByQuestionIndex(int examId);
}
