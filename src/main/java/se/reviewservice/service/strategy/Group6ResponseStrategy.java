package se.reviewservice.service.strategy;

import org.springframework.stereotype.Component;
import se.reviewservice.dto.Group6Review;
import se.reviewservice.model.Review;

import java.time.format.DateTimeFormatter;
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
        return reviews.stream()
                .map(review -> mapToGroup6Review(review))
                .collect(Collectors.toList());
    }

    private Group6Review mapToGroup6Review(Review review) {
        Group6Review g6Review = new Group6Review();

        // EXAKT de 4 fält som Grupp 6 vill ha
        g6Review.setSnittbetyg(review.getRating());              // Snittbetyg 1-5 (stjärnor)
        g6Review.setSkriftligReview(review.getComment());        // Skriftlig review
        g6Review.setNamnPaReviewer(review.getReviewerName());    // Namn på reviewer

        // Datum/tid när review skrevs - format: 2025-05-22 14:30:15
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        g6Review.setDatumTid(review.getDate().atStartOfDay().format(formatter));

        return g6Review;
    }
}