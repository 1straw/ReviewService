package se.reviewservice.mongoDB.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.mongoDB.service.ProductReviewService;
@Hidden
@RestController
@RequestMapping("/api/v1/products")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    /**
     * Gemensam endpoint för alla grupper.
     * Gruppidentifiering sker via API-nyckeln, så vi behöver inte gruppparameter.
     */
    @GetMapping("/reviews")
    public ResponseEntity<?> getProductReviews(
            @RequestParam(required = false) String productId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        // Hämta användarens autentisering
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        // Hämta gruppnamnet från användaren
        String groupName = auth.getName();
        System.out.println("Användare från autentisering: " + groupName);

        // Anropa service med gruppnamnet
        return productReviewService.getReviewsForGroup(productId, groupName, limit, offset);
    }

    /**
     * Endpoint för att hämta alla recensioner för en grupp
     * Identifierar gruppen automatiskt från API-nyckeln
     */
    @GetMapping("/all-reviews")
    public ResponseEntity<?> getAllReviewsForGroup(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        // Hämta användarens autentisering
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        // Hämta gruppnamnet från användaren
        String groupName = auth.getName();
        System.out.println("Användare från autentisering: " + groupName);

        // Anropa service med gruppnamnet
        return productReviewService.getAllReviewsForGroup(groupName, limit, offset);
    }
}