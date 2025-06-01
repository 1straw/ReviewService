package se.reviewservice.mongoDB.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.reviewservice.mongoDB.model.Product;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.repository.ProductRepository;
import se.reviewservice.mongoDB.repository.ReviewRepository;
import se.reviewservice.mongoDB.service.strategy.Group4ResponseStrategy;
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
        List<Review> reviewsById = reviewRepository.findByProductId(product.getId());
        reviews.addAll(reviewsById);

        // Metod 2: Sök med produktnamn
        List<Review> reviewsByName = new ArrayList<>();
        if (product.getName() != null && !product.getName().isEmpty()) {
            reviewsByName = reviewRepository.findByProductId(product.getName());
            reviews.addAll(reviewsByName);
        }

        // Om inga recensioner finns, använd AI för att generera
        if (reviews.isEmpty()) {
            String weather = "soligt och " + (20 + (int)(Math.random() * 10)) + " grader";
            aiReviewService.generateReviewWithGroup(product.getName(), weather, normalizedGroup, product.getId());

            // Hämta de nya recensionerna från databasen
            reviewsById = reviewRepository.findByProductId(product.getId());
            reviews = new ArrayList<>(reviewsById);

            reviewsByName = new ArrayList<>();
            if (product.getName() != null && !product.getName().isEmpty()) {
                reviewsByName = reviewRepository.findByProductId(product.getName());
                reviews.addAll(reviewsByName);
            }
        }

        // Filtrera recensioner baserat på limit och offset
        List<Review> paginatedReviews = reviews.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

        // Hitta rätt strategy för gruppen
        GroupResponseStrategy matchingStrategy = findStrategyForGroup(normalizedGroup);

        if (matchingStrategy == null) {
            return ResponseEntity.badRequest().body("Unsupported group: " + normalizedGroup);
        }

        // SPECIELL HANTERING FÖR GRUPP 5 - returnera bara lista med recensioner
        if (normalizedGroup.equals("group5") || normalizedGroup.equals("group6")) {
            Object formattedReviews = matchingStrategy.buildResponse(paginatedReviews, product.getId());
            return ResponseEntity.ok(formattedReviews);
        }

        // För andra grupper, förbereder svaret med produktinfo
        Map<String, Object> response = new HashMap<>();

        // Lägger till produktinformation baserat på vilken grupp det är
        response.put("id", product.getId());
        response.put("name", product.getName());
        response.put("description", product.getDescription());

        // Lägg bara till pris om det INTE är grupp 4
        if (!(matchingStrategy instanceof Group4ResponseStrategy)) {
            response.put("price", product.getPrice());
        }

        // Formaterar recensioner enligt gruppens strategy
        Object formattedReviews = matchingStrategy.buildResponse(paginatedReviews, product.getId());
        response.put("reviews", formattedReviews);

        return ResponseEntity.ok(response);
    }

    /**
     * Hämta alla recensioner för en specifik grupp
     */
    public ResponseEntity<?> getAllReviewsForGroup(String group, int limit, int offset) {
        // Normalisera gruppnamnet
        String normalizedGroup = normalizeGroupName(group);

        // Hämta alla produkter för gruppen
        List<Product> products = productRepository.findByGroupId(normalizedGroup);

        if (products.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        // SPECIELL HANTERING FÖR GRUPP 5 och GRUPP 6
        if (normalizedGroup.equals("group5") || normalizedGroup.equals("group6")) {
            return handleFlatReviews(products, normalizedGroup, limit, offset);
        }

        // Resten av koden för andra grupper (Grupp 4)
        return handleOtherGroupReviews(products, normalizedGroup, limit, offset);
    }

    private ResponseEntity<?> handleFlatReviews(List<Product> products, String normalizedGroup, int limit, int offset) {
        List<Review> allReviews = new ArrayList<>();

        for (Product product : products) {
            // Hämta recensioner för produkten
            List<Review> reviews = getReviewsForProduct(product, normalizedGroup);
            allReviews.addAll(reviews);
        }

        // Applicera pagination på alla recensioner
        List<Review> paginatedReviews = allReviews.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

        // Formatera recensioner enligt GroupResponseStrategy
        GroupResponseStrategy strategy = findStrategyForGroup(normalizedGroup);

        if (strategy != null) {
            Object formattedReviews = strategy.buildResponse(paginatedReviews, null);
            return ResponseEntity.ok(formattedReviews);
        }

        return ResponseEntity.ok(new ArrayList<>());
    }

    private ResponseEntity<?> handleOtherGroupReviews(List<Product> products, String normalizedGroup, int limit, int offset) {
        GroupResponseStrategy strategy = findStrategyForGroup(normalizedGroup);

        if (strategy == null) {
            return ResponseEntity.badRequest().body("Unsupported group: " + normalizedGroup);
        }

        // Skapa svarsstrukturen baserat på gruppens typ
        List<Map<String, Object>> result = new ArrayList<>();

        // För varje produkt, hämta recensioner och formatera enligt gruppens krav
        for (Product product : products) {
            List<Review> reviews = getReviewsForProduct(product, normalizedGroup);

            if (reviews.isEmpty()) {
                continue;
            }

            // Skapa produktobjektet med recensioner
            Map<String, Object> productData = new HashMap<>();

            // Gemensam produktinformation för alla grupper (utom Grupp 5 och 6)
            productData.put("id", product.getId());
            productData.put("name", product.getName());
            productData.put("description", product.getDescription());

            // Lägg bara till pris om det INTE är grupp 4
            if (!(strategy instanceof Group4ResponseStrategy)) {
                productData.put("price", product.getPrice());
            }

            // Formatera recensioner enligt gruppens strategy
            Object formattedReviews = strategy.buildResponse(reviews, product.getId());
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

    private List<Review> getReviewsForProduct(Product product, String normalizedGroup) {
        List<Review> reviews = new ArrayList<>();

        // Hämta recensioner från databasen
        List<Review> reviewsById = reviewRepository.findByProductId(product.getId());
        reviews.addAll(reviewsById);

        if (product.getName() != null && !product.getName().isEmpty()) {
            List<Review> reviewsByName = reviewRepository.findByProductId(product.getName());
            reviews.addAll(reviewsByName);
        }

        // Om inga recensioner finns, generera nya
        if (reviews.isEmpty()) {
            String weather = "soligt och " + (20 + (int)(Math.random() * 10)) + " grader";
            aiReviewService.generateReviewWithGroup(product.getName(), weather, normalizedGroup, product.getId());

            reviewsById = reviewRepository.findByProductId(product.getId());
            reviews = new ArrayList<>(reviewsById);

            if (product.getName() != null && !product.getName().isEmpty()) {
                List<Review> reviewsByName = reviewRepository.findByProductId(product.getName());
                reviews.addAll(reviewsByName);
            }
        }

        return reviews;
    }

    private GroupResponseStrategy findStrategyForGroup(String normalizedGroup) {
        for (GroupResponseStrategy s : responseStrategies) {
            if (s.supports(normalizedGroup)) {
                return s;
            }
        }
        return null;
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