package se.reviewservice.mongoDB.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.reviewservice.dto.ReviewRequest;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.repository.ReviewRepository;
import se.reviewservice.mongoDB.service.strategy.GroupResponseStrategy;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private List<GroupResponseStrategy> responseStrategies;

    @Autowired
    private AiReviewService aiReviewService;

    public ResponseEntity<?> getReviewsForGroup(String productId, String group, int limit, int offset) {
        // Hämta recensioner från databasen
        List<Review> reviews = reviewRepository.findByProductId(productId);

        // Om inga recensioner finns eller för få, använd AI för att generera
        if (reviews.isEmpty() || reviews.size() < 5) {
            // Skapa en enkel vädertext - i ett riktigt system skulle detta komma från en vädertjänst
            String weather = "soligt och " + (20 + (int)(Math.random() * 10)) + " grader";

            // Använd befintlig AI-tjänst för att generera recensioner
            ReviewRequest request = new ReviewRequest();
            request.setProductName(productId);  // Använd produktID som namn
            request.setWeather(weather);

            aiReviewService.generateReview(productId, weather);

            // Hämta de nya recensionerna från databasen
            reviews = reviewRepository.findByProductId(productId);
        }

        // Filtrera recensioner baserat på limit och offset
        List<Review> paginatedReviews = reviews.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

        // Hitta rätt strategy för gruppen
        for (GroupResponseStrategy strategy : responseStrategies) {
            if (strategy.supports(group)) {
                Object response = strategy.buildResponse(paginatedReviews, productId);
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.badRequest().body("Unsupported group: " + group);
    }
}