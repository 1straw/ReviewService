package se.reviewservice.mongoDB.service.strategy;

import org.springframework.stereotype.Component;
import se.reviewservice.dto.Group5Review;
import se.reviewservice.mongoDB.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Group5ResponseStrategy implements GroupResponseStrategy {

    @Override
    public boolean supports(String group) {
        return "group5".equalsIgnoreCase(group);
    }

    @Override
    public Object buildResponse(List<Review> reviews, String productId) {
        return reviews.stream()
                .map(review -> mapToGroup5Review(review, productId))
                .collect(Collectors.toList());
    }

    private Group5Review mapToGroup5Review(Review review, String productId) {
        Group5Review g5Review = new Group5Review();
        g5Review.setReviewId(review.getId());
        g5Review.setProductId(productId);
        g5Review.setReviewerName(review.getReviewerName());
        g5Review.setReviewTitle(review.getTitle());
        g5Review.setReviewContent(review.getComment());
        g5Review.setRating(review.getRating());
        g5Review.setCreationDate(review.getDate());
        return g5Review;
    }
}