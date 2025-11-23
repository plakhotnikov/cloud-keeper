package com.plakhotnikov.cloudkeeper.user.repository;

import com.plakhotnikov.cloudkeeper.user.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile({"auth"})
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
