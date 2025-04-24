package se.reviewservice.mongoDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import se.reviewservice.mongoDB.model.Review;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
}
