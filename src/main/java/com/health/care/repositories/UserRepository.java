package com.health.care.repositories;

import java.util.Optional;

import com.health.care.entities.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<UserDocument> findByEmailIgnoreCase(String email);

    Optional<UserDocument> findByResetToken(String resetToken);
}
