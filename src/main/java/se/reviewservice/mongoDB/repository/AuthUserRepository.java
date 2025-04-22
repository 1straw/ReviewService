package se.reviewservice.mongoDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.reviewservice.mongoDB.model.AuthUser;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends MongoRepository<AuthUser, String> {
    Optional<AuthUser> findByUsername(String username);
    Optional<AuthUser> findByApiKey(String apiKey);
    boolean existsByUsername(String username);
    boolean existsByApiKey(String apiKey);
}