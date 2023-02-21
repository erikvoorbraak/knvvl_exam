package org.knvvl.exam.repos;

import java.util.List;

import org.knvvl.exam.entities.Change;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChangeRepository extends JpaRepository<Change, Change.ChangeKey>
{
    List<Change> findByChangeKeyQuestionIdOrderByChangeKeyChangedAtDesc(int question);
}
