package se.reviewservice.mongoDB.service.strategy;

import se.reviewservice.mongoDB.model.Review;
import java.util.List;

public interface GroupResponseStrategy {
    Object buildResponse(List<Review> reviews, String productId);
    boolean supports(String group);
}