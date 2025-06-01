package se.reviewservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.model.Product;
import se.reviewservice.model.Review;
import se.reviewservice.repository.ProductRepository;
import se.reviewservice.repository.ReviewRepository;
import se.reviewservice.service.ProductReviewService;
import se.reviewservice.service.strategy.GroupResponseStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Group Reviews", description = "API för att hantera grupperade recensioner")
public class GroupedReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private List<GroupResponseStrategy> responseStrategies;

    @GetMapping("/group-reviews")
    @Operation(summary = "Hämta recensioner för en specifik grupp",
            description = "Returnerar alla recensioner för produkter som tillhör den angivna gruppen")
    public ResponseEntity<?> getReviewsForGroup(@RequestParam String group) {
        // Kontrollera att användaren har rätt att hämta denna grupps data
        if (!isUserAuthorizedForGroup(group)) {
            return ResponseEntity.status(403).body("Access denied: You can only access your own group's data");
        }

        // Normalisera gruppnamnet
        String normalizedGroup = normalizeGroupName(group);

        List<Product> groupProducts = productRepository.findByGroupId(normalizedGroup);
        List<Map<String, Object>> productReviews = new ArrayList<>();

        for (Product product : groupProducts) {
            // Sök recensioner med både produkt-ID OCH produktnamn
            List<Review> reviews = new ArrayList<>();

            // Metod 1: Sök med produkt-ID
            List<Review> reviewsById = reviewRepository.findByProductId(product.getId());
            reviews.addAll(reviewsById);

            // Metod 2: Sök med produktnamn
            if (product.getName() != null) {
                List<Review> reviewsByName = reviewRepository.findByProductId(product.getName());
                reviews.addAll(reviewsByName);
            }

            if (!reviews.isEmpty()) {
                for (GroupResponseStrategy strategy : responseStrategies) {
                    if (strategy.supports(normalizedGroup)) {
                        Object formattedReviews = strategy.buildResponse(reviews, product.getId());

                        Map<String, Object> productData = new HashMap<>();
                        productData.put("productId", product.getId());
                        productData.put("productName", product.getName());
                        productData.put("reviews", formattedReviews);

                        productReviews.add(productData);
                        break;
                    }
                }
            }
        }

        return ResponseEntity.ok(productReviews);
    }
    @Hidden
    @GetMapping("/products/reviews")
    public ResponseEntity<?> getProductReviews(
            @RequestParam String productId,
            @RequestParam String group,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        // Kontrollera att användaren har rätt att hämta denna grupps data
        if (!isUserAuthorizedForGroup(group)) {
            return ResponseEntity.status(403).body("Access denied: You can only access your own group's data");
        }

        return productReviewService.getReviewsForGroup(productId, group, limit, offset);
    }
    @Hidden
    @GetMapping("/products/all-reviews")
    public ResponseEntity<?> getAllReviews(
            @RequestParam(required = false) String group,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        // Om ingen grupp anges, använd användarens grupp
        if (group == null || group.trim().isEmpty()) {
            group = getUserGroupFromAuthentication();
            if (group == null) {
                return ResponseEntity.badRequest().body("Group parameter is required or user authentication is invalid");
            }
        } else {
            // Om grupp anges, kontrollera att användaren har rätt att hämta den gruppens data
            if (!isUserAuthorizedForGroup(group)) {
                return ResponseEntity.status(403).body("Access denied: You can only access your own group's data");
            }
        }

        return productReviewService.getAllReviewsForGroup(group, limit, offset);
    }

    /**
     * Kontrollerar om den autentiserade användaren har rätt att komma åt den angivna gruppens data
     */
    private boolean isUserAuthorizedForGroup(String requestedGroup) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Normalisera den begärda gruppen
        String normalizedRequestedGroup = normalizeGroupName(requestedGroup);

        // Kontrollera om användaren har ROLE_ADMIN (kan komma åt alla grupper)
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ADMIN"))) {
            return true;
        }

        // Kontrollera direkt om användarnamnet matchar gruppen
        String username = authentication.getName().toLowerCase();
        if (username.equals(normalizedRequestedGroup)) {
            return true;
        }

        // Kontrollera om användaren har rätt att komma åt den specifika gruppen
        String[] rolePrefixes = {"ROLE_", "GROUP_", ""};
        String[] groupPrefixes = {"GROUP", ""};

        for (String rolePrefix : rolePrefixes) {
            for (String groupPrefix : groupPrefixes) {
                String expectedRole = rolePrefix + groupPrefix + normalizedRequestedGroup.toUpperCase();

                boolean hasRole = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(expectedRole));

                if (hasRole) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Hämtar användarens grupp från autentiseringen
     */
    private String getUserGroupFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // Först, om användarnamnet själv är en grupp, använd det
        String username = authentication.getName().toLowerCase();
        if (username.startsWith("group")) {
            return username;
        }

        // Leta efter roller som börjar med GROUP eller ROLE_GROUP
        return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(authority -> {
                    return authority.startsWith("ROLE_GROUP") ||
                            authority.startsWith("GROUP") ||
                            authority.equals("ROLE_GROUP4") ||
                            authority.equals("ROLE_GROUP5") ||
                            authority.equals("ROLE_GROUP6");
                })
                .map(authority -> {
                    if (authority.startsWith("ROLE_GROUP")) {
                        return authority.substring("ROLE_GROUP".length()).toLowerCase();
                    }
                    if (authority.startsWith("GROUP")) {
                        return authority.substring("GROUP".length()).toLowerCase();
                    }
                    if (authority.equals("ROLE_GROUP4")) return "group4";
                    if (authority.equals("ROLE_GROUP5")) return "group5";
                    if (authority.equals("ROLE_GROUP6")) return "group6";
                    return authority.toLowerCase();
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Normaliserar gruppnamn för jämförelse
     */
    private String normalizeGroupName(String group) {
        if (group == null) return null;

        // Ta bort ROLE_ prefix om det finns
        if (group.startsWith("ROLE_")) {
            group = group.substring(5);
        }

        // Ta även bort GROUP_ prefix om det finns
        if (group.startsWith("GROUP_")) {
            group = group.substring(6);
        }

        // Lägg till "group" prefix om det saknas och är en siffra
        if (group.matches("^\\d+$")) {
            group = "group" + group;
        }

        return group.toLowerCase();
    }
}