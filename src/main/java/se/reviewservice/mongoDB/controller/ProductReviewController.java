package se.reviewservice.mongoDB.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.mongoDB.service.ProductReviewService;

@RestController
@RequestMapping("/api/v1/products")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<?> getProductReviews(
            @PathVariable String productId,
            @RequestParam(required = true) String group,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        return productReviewService.getReviewsForGroup(productId, group, limit, offset);
    }
}