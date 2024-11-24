package org.knvvl.exam.repos;

import java.util.List;

import org.knvvl.exam.entities.Question;
import org.knvvl.exam.entities.Topic;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer>
{
    Question findTopByOrderByIdDesc();

    List<Question> findByTopicOrderById(Topic topic);

    List<Question> findByLanguage(String language, Sort sort);
}
