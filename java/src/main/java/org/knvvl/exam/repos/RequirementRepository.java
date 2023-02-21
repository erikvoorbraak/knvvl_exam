package org.knvvl.exam.repos;

import org.knvvl.exam.entities.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Integer>
{
    Requirement findTopByOrderByIdDesc();
}
