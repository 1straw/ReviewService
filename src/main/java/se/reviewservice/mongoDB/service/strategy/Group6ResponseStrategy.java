package se.reviewservice.mongoDB.service.strategy;

import org.springframework.stereotype.Component;
import se.reviewservice.dto.Group6Response;
import se.reviewservice.dto.Group6Review;
import se.reviewservice.mongoDB.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Group6ResponseStrategy implements GroupResponseStrategy {

    @Override
    public boolean supports(String group) {
        // Normalisera gruppnamnet
        if (group == null) return false;

        String normalizedGroup = group.toLowerCase();
        if (normalizedGroup.startsWith("role_")) {
            normalizedGroup = normalizedGroup.substring(5);
        }

        return "group6".equals(normalizedGroup);
    }

    @Override
    public Object buildResponse(List<Review> reviews, String productId) {
        // Beräkna genomsnittligt betyg
        double averageRating = reviews.isEmpty() ? 0.0 :
                reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);

        // Mappa recensioner till Group6Review format
        List<Group6Review> group6Reviews = reviews.stream()
                .map(this::mapToGroup6Review)
                .collect(Collectors.toList());

        // Skapa responsobjekt
        Group6Response response = new Group6Response();
        response.setAverageRating(averageRating);
        response.setReviews(group6Reviews);

        return response;
    }

    private Group6Review mapToGroup6Review(Review review) {
        Group6Review g6Review = new Group6Review();
        g6Review.setText(review.getComment());
        g6Review.setRating(review.getRating());
        g6Review.setReviewerName(review.getReviewerName());
        g6Review.setReviewDate(review.getDate());
        return g6Review;
    }
}