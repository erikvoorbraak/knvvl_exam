package org.knvvl.exam.repos;

import org.knvvl.exam.entities.Text;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface TextRepository extends JpaRepository<Text, String>
{
    @Transactional
    @Override
    <S extends Text> S save(S text);
}
