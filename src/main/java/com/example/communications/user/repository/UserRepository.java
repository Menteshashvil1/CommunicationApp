package com.example.communications.user.repository;

import com.example.communications.user.model.User;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(@Email String email);
    Optional<User> findByEmail(@Email String email);

}
