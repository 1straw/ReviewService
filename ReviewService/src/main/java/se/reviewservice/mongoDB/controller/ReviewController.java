package se.reviewservice.mongoDB.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.service.ReviewService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    @Autowired
    ReviewService service;

    @GetMapping()
    public List<Review> getAllReviews() {
        return service.getAllReviews();
    }

    @GetMapping("{id}")
    public Optional<Review> getReviewById(@PathVariable String id) {
        return service.getReviewById(id);
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return service.createReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable String id) {
        service.deleteReview(id);
    }
}
