package org.knvvl.exam.repos;

import org.knvvl.exam.entities.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer>
{
    Topic findTopByOrderByIdDesc();
}
