package se.reviewservice.mongoDB.controller;

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
        List<Product> groupProducts = productRepository.findByGroupId(group);

        List<Map<String, Object>> productReviews = new ArrayList<>();

        for (Product product : groupProducts) {
            List<Review> reviews = reviewRepository.findByProductId(product.getId());

            for (GroupResponseStrategy strategy : responseStrategies) {
                if (strategy.supports(group)) {
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

        return ResponseEntity.ok(productReviews);
    }
}