package org.knvvl.exam.repos;

import org.knvvl.exam.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer>
{
    User findTopByOrderByIdDesc();

    User findByUsername(String username);
}
