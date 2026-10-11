package org.knvvl.exam.repos;

import java.util.List;

import org.knvvl.exam.entities.Change;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.annotation.Nullable;

public interface ChangeRepository extends JpaRepository<Change, Change.ChangeKey>
{
    List<Change> findByChangeKeyQuestionIdOrderByChangeKeyChangedAtDesc(int question);

    @Nullable
    Change findTopByChangeKeyQuestionIdOrderByChangeKeyChangedAtDesc(int question);
}
