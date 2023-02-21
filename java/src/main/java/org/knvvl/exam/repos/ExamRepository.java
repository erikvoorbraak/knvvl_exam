package org.knvvl.exam.repos;

import java.util.List;

import org.knvvl.exam.entities.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Integer>
{
    List<Exam.ExamView> getExamByOrderByIdDesc();

    Exam findTopByOrderByIdDesc();

    Exam findByLabel(String label);
}
