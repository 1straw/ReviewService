package se.reviewservice.mongoDB.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.reviewservice.dto.ReviewRequest;
import se.reviewservice.mongoDB.model.Product;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.repository.ProductRepository;
import se.reviewservice.mongoDB.repository.ReviewRepository;
import se.reviewservice.mongoDB.service.strategy.GroupResponseStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private List<GroupResponseStrategy> responseStrategies;

    @Autowired
    private AiReviewService aiReviewService;

    /**
     * Hämta recensioner för en specifik produkt och grupp
     */
    public ResponseEntity<?> getReviewsForGroup(String productId, String group, int limit, int offset) {
        // Normalisera gruppnamnet
        String normalizedGroup = normalizeGroupName(group);
        System.out.println("getReviewsForGroup - productId: " + productId + ", group: " + normalizedGroup);

        if (productId == null || productId.isEmpty()) {
            return ResponseEntity.badRequest().body("Product ID is required");
        }

        // Hämta produkten för att kontrollera att den tillhör gruppens produkter
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        // Kontrollera att produkten tillhör den angivna gruppen
        if (!normalizeGroupName(product.getGroupId()).equals(normalizedGroup)) {
            return ResponseEntity.status(403).body("This product does not belong to your group");
        }

        // Hämta recensioner från databasen - sök med både produkt-ID OCH produktnamn
        List<Review> reviews = new ArrayList<>();

        // Metod 1: Sök med produkt-ID
        List<Review> reviewsById = reviewRepository.findByProductId(productId);
        reviews.addAll(reviewsById);

        // Metod 2: Sök med produktnamn
        List<Review> reviewsByName = new ArrayList<>();
        if (product.getName() != null) {
            reviewsByName = reviewRepository.findByProductId(product.getName());
            reviews.addAll(reviewsByName);
        }

        System.out.println("Hittade " + reviewsById.size() + " recensioner med produkt-ID");
        System.out.println("Hittade " + reviewsByName.size() + " recensioner med produktnamn");
        System.out.println("Totalt " + reviews.size() + " recensioner");

        // Om inga recensioner finns eller för få, använd AI för att generera
        if (reviews.isEmpty() || reviews.size() < 5) {
            // Skapa en enkel vädertext - i ett riktigt system skulle detta komma från en vädertjänst
            String weather = "soligt och " + (20 + (int)(Math.random() * 10)) + " grader";

            // Använd befintlig AI-tjänst för att generera recensioner
            aiReviewService.generateReview(product.getName(), weather);

            // Hämta de nya recensionerna från databasen
            reviewsById = reviewRepository.findByProductId(productId);
            reviewsByName = new ArrayList<>();

            reviews = new ArrayList<>(reviewsById);

            if (product.getName() != null) {
                reviewsByName = reviewRepository.findByProductId(product.getName());
                reviews.addAll(reviewsByName);
            }

            System.out.println("Efter generering, hittade " + reviews.size() + " recensioner");
        }

        // Filtrera recensioner baserat på limit och offset
        List<Review> paginatedReviews = reviews.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

        // Hitta rätt strategy för gruppen
        GroupResponseStrategy matchingStrategy = null;
        for (GroupResponseStrategy strategy : responseStrategies) {
            if (strategy.supports(normalizedGroup)) {
                matchingStrategy = strategy;
                System.out.println("Använder strategy: " + strategy.getClass().getSimpleName());
                break;
            }
        }

        if (matchingStrategy == null) {
            System.out.println("Ingen matchande strategy hittades för grupp: " + normalizedGroup);
            return ResponseEntity.badRequest().body("Unsupported group: " + normalizedGroup);
        }

        Object response = matchingStrategy.buildResponse(paginatedReviews, productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Hämta alla recensioner för en specifik grupp
     */
    public ResponseEntity<?> getAllReviewsForGroup(String group, int limit, int offset) {
        // Normalisera gruppnamnet
        String normalizedGroup = normalizeGroupName(group);
        System.out.println("getAllReviewsForGroup - group: " + normalizedGroup);

        // Hämta alla produkter för gruppen
        List<Product> products = productRepository.findByGroupId(normalizedGroup);
        System.out.println("Hittade " + products.size() + " produkter för grupp " + normalizedGroup);

        if (products.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        // Skapa svarsstrukturen
        List<Map<String, Object>> result = new ArrayList<>();

        // Hitta rätt strategy för gruppen
        GroupResponseStrategy strategy = null;
        for (GroupResponseStrategy s : responseStrategies) {
            if (s.supports(normalizedGroup)) {
                strategy = s;
                System.out.println("Använder strategy: " + s.getClass().getSimpleName());
                break;
            }
        }

        if (strategy == null) {
            System.out.println("Ingen matchande strategy hittades för grupp: " + normalizedGroup);
            return ResponseEntity.badRequest().body("Unsupported group: " + normalizedGroup);
        }

        // För varje produkt, hämta recensioner och formatera enligt gruppens krav
        for (Product product : products) {
            System.out.println("Behandlar produkt: " + product.getId() + " - " + product.getName());

            // Hämta recensioner från databasen - sök med både produkt-ID OCH produktnamn
            List<Review> reviews = new ArrayList<>();

            // Metod 1: Sök med produkt-ID
            List<Review> reviewsById = reviewRepository.findByProductId(product.getId());
            reviews.addAll(reviewsById);

            // Metod 2: Sök med produktnamn
            List<Review> reviewsByName = new ArrayList<>();
            if (product.getName() != null) {
                reviewsByName = reviewRepository.findByProductId(product.getName());
                reviews.addAll(reviewsByName);
            }

            System.out.println("  Hittade " + reviewsById.size() + " recensioner med produkt-ID");
            System.out.println("  Hittade " + reviewsByName.size() + " recensioner med produktnamn");
            System.out.println("  Totalt " + reviews.size() + " recensioner");

            // Om inga recensioner finns, fortsätt till nästa produkt
            if (reviews.isEmpty()) {
                System.out.println("  Inga recensioner hittades, hoppar över produkt");
                continue;
            }

            // Formatera recensioner enligt gruppens strategy
            Object formattedReviews = strategy.buildResponse(reviews, product.getId());

            // Skapa produktobjektet med recensioner
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", product.getId());
            productData.put("name", product.getName());
            productData.put("description", product.getDescription());
            productData.put("price", product.getPrice());
            productData.put("reviews", formattedReviews);

            result.add(productData);
        }

        // Applicera pagination på slutresultatet om det behövs
        if (offset > 0 || limit < result.size()) {
            int endIndex = Math.min(offset + limit, result.size());
            if (offset < result.size()) {
                result = result.subList(offset, endIndex);
            } else {
                result = new ArrayList<>();
            }
        }

        return ResponseEntity.ok(result);
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