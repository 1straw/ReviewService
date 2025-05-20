package se.reviewservice.mongoDB.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.mongoDB.model.Product;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.repository.ProductRepository;
import se.reviewservice.mongoDB.repository.ReviewRepository;
import se.reviewservice.mongoDB.service.AiReviewService;
import se.reviewservice.mongoDB.service.ProductFetchService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@Hidden
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductFetchService productFetchService;

    @Autowired
    private AiReviewService aiReviewService;

    @GetMapping("/cleanup")
    public ResponseEntity<?> cleanupProducts(@RequestParam String group) {
        // Ta bort alla produkter för gruppen
        List<Product> products = productRepository.findByGroupId(group);
        int count = products.size();

        for (Product product : products) {
            productRepository.delete(product);
        }

        return ResponseEntity.ok("Deleted " + count + " products for group " + group);
    }

    @GetMapping("/fetch")
    public ResponseEntity<?> fetchProducts(@RequestParam String group) {
        try {
            productFetchService.manualFetchAllProducts();
            return ResponseEntity.ok("Started product fetch for all groups");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/generate-test-products")
    public ResponseEntity<?> generateTestProducts(@RequestParam String group, @RequestParam int count) {
        for (int i = 0; i < count; i++) {
            Product product = new Product();
            product.setId(UUID.randomUUID().toString());
            product.setName("Test Product " + (i + 1));
            product.setDescription("Test product for " + group);
            product.setPrice(new BigDecimal("199.99"));
            product.setGroupId(group);
            product.setAttributes(Map.of("category", "Test", "source", group));

            productRepository.save(product);
        }

        return ResponseEntity.ok("Generated " + count + " test products for group " + group);
    }

    @GetMapping("/generate-reviews")
    public ResponseEntity<?> generateReviews(@RequestParam String group) {
        List<Product> products = productRepository.findByGroupId(group);

        if (products.isEmpty()) {
            return ResponseEntity.badRequest().body("No products found for group " + group);
        }

        int count = 0;
        for (Product product : products) {
            // Kalla på AiReviewService för att generera recensioner
            String result = aiReviewService.generateReview(product.getName(), "soligt");
            count++;
        }

        return ResponseEntity.ok("Generated reviews for " + count + " products for group " + group);
    }

    @GetMapping("/list-reviews")
    public ResponseEntity<?> listReviews(@RequestParam(required = false) String productId) {
        List<Review> reviews;

        if (productId != null && !productId.isEmpty()) {
            reviews = reviewRepository.findByProductId(productId);
        } else {
            reviews = reviewRepository.findAll();
        }

        return ResponseEntity.ok(Map.of(
                "count", reviews.size(),
                "reviews", reviews
        ));
    }

    @GetMapping("/list-products")
    public ResponseEntity<?> listProducts(@RequestParam(required = false) String group) {
        List<Product> products;

        if (group != null && !group.isEmpty()) {
            products = productRepository.findByGroupId(group);
        } else {
            products = productRepository.findAll();
        }

        return ResponseEntity.ok(Map.of(
                "count", products.size(),
                "products", products
        ));
    }
}