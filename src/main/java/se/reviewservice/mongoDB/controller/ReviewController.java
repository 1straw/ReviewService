package se.reviewservice.mongoDB.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.service.ReviewService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "API för att hantera recensioner")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "apiKey")
public class ReviewController {
    @Autowired
    ReviewService service;

    @Operation(summary = "Get reviews", description = "Get all reviews")
    @GetMapping()
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = service.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get review", description = "Get review by id")
    @GetMapping("{id}")
    public ResponseEntity<Optional<Review>> getReviewById(@PathVariable String id) {
        Optional<Review> review = service.getReviewById(id);
        if (review.isPresent()) {
            return ResponseEntity.ok(review); // 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404 Not Found
        }
    }

    @Operation( hidden = true,
            summary = "Post review",
            description = "Create a review",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "apiKey")
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Review createdReview = service.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview); // 201 Created
    }

    @Operation(hidden = true ,
            summary = "Delete review",
            description = "Delete review by id",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "apiKey")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        Optional<Review> review = service.getReviewById(id);
        if (review.isPresent()) {
            service.deleteReview(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        }
    }
}