package se.reviewservice.mongoDB.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.mongoDB.model.Product;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.repository.ProductRepository;
import se.reviewservice.mongoDB.repository.ReviewRepository;
import se.reviewservice.mongoDB.service.strategy.GroupResponseStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Hidden
@RestController
@RequestMapping("/api/v1")
public class GroupReviewController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private List<GroupResponseStrategy> responseStrategies;

    @GetMapping("/group-reviews")
    public ResponseEntity<?> getReviewsForGroup(@RequestParam String group) {
        // Normalisera gruppnamnet
        String normalizedGroup = normalizeGroupName(group);
        System.out.println("Söker recensioner för grupp: " + normalizedGroup);

        List<Product> groupProducts = productRepository.findByGroupId(normalizedGroup);
        System.out.println("Hittade " + groupProducts.size() + " produkter för grupp " + normalizedGroup);

        List<Map<String, Object>> productReviews = new ArrayList<>();

        for (Product product : groupProducts) {  // Ändrat från "products" till "groupProducts"
            System.out.println("Behandlar produkt: " + product.getId() + " - " + product.getName());

            // Sök recensioner med både produkt-ID OCH produktnamn
            List<Review> reviews = new ArrayList<>();

            // Metod 1: Sök med produkt-ID
            List<Review> reviewsById = reviewRepository.findByProductId(product.getId());
            System.out.println("  Hittade " + reviewsById.size() + " recensioner med produkt-ID");
            reviews.addAll(reviewsById);

            // Metod 2: Sök med produktnamn
            if (product.getName() != null) {
                List<Review> reviewsByName = reviewRepository.findByProductId(product.getName());
                System.out.println("  Hittade " + reviewsByName.size() + " recensioner med produktnamn");
                reviews.addAll(reviewsByName);
            }

            if (!reviews.isEmpty()) {
                System.out.println("  Totalt " + reviews.size() + " recensioner för denna produkt");

                for (GroupResponseStrategy strategy : responseStrategies) {
                    if (strategy.supports(normalizedGroup)) {
                        System.out.println("  Använder strategy: " + strategy.getClass().getSimpleName());
                        Object formattedReviews = strategy.buildResponse(reviews, product.getId());

                        Map<String, Object> productData = new HashMap<>();
                        productData.put("productId", product.getId());
                        productData.put("productName", product.getName());
                        productData.put("reviews", formattedReviews);

                        productReviews.add(productData);
                        break;
                    }
                }
            } else {
                System.out.println("  Inga recensioner hittades för denna produkt");
            }
        }

        return ResponseEntity.ok(productReviews);
    }

    // Hjälpmetod för att normalisera gruppnamn
    private String normalizeGroupName(String group) {
        if (group == null) return null;

        // Om gruppen har prefixet "ROLE_", ta bort det
        if (group.startsWith("ROLE_")) {
            group = group.substring(5);
        }

        // Konvertera till gemener för att göra jämförelsen mindre känslig
        return group.toLowerCase();
    }
}