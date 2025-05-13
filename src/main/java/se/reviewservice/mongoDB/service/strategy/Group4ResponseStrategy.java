package se.reviewservice.mongoDB.service.strategy;

import org.springframework.stereotype.Component;
import se.reviewservice.dto.Group4Response;
import se.reviewservice.dto.Group4Review;
import se.reviewservice.mongoDB.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Group4ResponseStrategy implements GroupResponseStrategy {

    @Override
    public boolean supports(String group) {
        return "group4".equalsIgnoreCase(group);
    }

    @Override
    public Object buildResponse(List<Review> reviews, String productId) {
        Group4Response response = new Group4Response();
        response.setAverageRating(calculateAverageRating(reviews));
        response.setTotalReviews(reviews.size());
        response.setReviews(mapToGroup4Reviews(reviews));
        return response;
    }

    private double calculateAverageRating(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    private List<Group4Review> mapToGroup4Reviews(List<Review> reviews) {
        return reviews.stream()
                .map(this::mapToGroup4Review)
                .collect(Collectors.toList());
    }

    private Group4Review mapToGroup4Review(Review review) {
        Group4Review g4Review = new Group4Review();
        g4Review.setReviewContent(review.getComment());
        g4Review.setRating(review.getRating());
        g4Review.setReviewerName(review.getReviewerName());
        g4Review.setDate(review.getDate());
        return g4Review;
    }
}