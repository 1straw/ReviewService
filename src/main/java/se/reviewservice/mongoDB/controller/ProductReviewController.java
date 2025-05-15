package se.reviewservice.mongoDB.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.mongoDB.service.ProductReviewService;

@RestController
@RequestMapping("/api/v1/products")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;
    @Hidden
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<?> getProductReviews(
            @PathVariable String productId,
            @RequestParam(required = true) String group,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        return productReviewService.getReviewsForGroup(productId, group, limit, offset);
    }
}