package com.furryhub.petservices.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furryhub.petservices.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> { 
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String verificationToken);
    Optional<User> findByResetToken(String resetToken);
}
