package se.reviewservice.service.strategy;

import org.springframework.stereotype.Component;
import se.reviewservice.dto.Group5Review;
import se.reviewservice.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Group5ResponseStrategy implements GroupResponseStrategy {

    @Override
    public boolean supports(String group) {
        // Normalisera gruppnamnet
        if (group == null) return false;

        String normalizedGroup = group.toLowerCase();
        if (normalizedGroup.startsWith("role_")) {
            normalizedGroup = normalizedGroup.substring(5);
        }

        return "group5".equals(normalizedGroup);
    }

    @Override
    public Object buildResponse(List<Review> reviews, String productId) {
        return reviews.stream()
                .map(review -> mapToGroup5Review(review, productId))
                .collect(Collectors.toList());
    }

    private Group5Review mapToGroup5Review(Review review, String productId) {
        Group5Review g5Review = new Group5Review();

        // ENDAST de 7 fält som Grupp 5 vill ha
        g5Review.setReviewId(review.getId());
        g5Review.setProductId(productId);
        g5Review.setReviewerName(formatReviewerName(review.getReviewerName()));
        g5Review.setReviewTitle(review.getTitle() != null ? review.getTitle() : "Review");
        g5Review.setReviewContent(review.getComment());
        g5Review.setRating(review.getRating());
        g5Review.setCreationDate(review.getDate().toString()); // Format: 2025-05-22

        return g5Review;
    }

    private String formatReviewerName(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] nameParts = fullName.split(" ");
            if (nameParts.length > 1) {
                return nameParts[0] + " " + nameParts[1].charAt(0) + ".";
            } else {
                return nameParts[0];
            }
        } else {
            return "Anonymous";
        }
    }
}