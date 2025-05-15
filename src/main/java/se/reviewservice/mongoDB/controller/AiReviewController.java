package se.reviewservice.mongoDB.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.dto.ExternalProductRequestWithWeather;
import se.reviewservice.dto.ExternalProductResponse;
import se.reviewservice.dto.IncomingProductRequest;
import se.reviewservice.dto.ReviewRequest;
import se.reviewservice.mongoDB.service.AiReviewService;
import se.reviewservice.mongoDB.service.ReviewService;

@Hidden
@RestController
@RequestMapping("/api/v1/ai")
public class AiReviewController {

    private final AiReviewService reviewService;

    public AiReviewController(AiReviewService reviewService) {
        this.reviewService = reviewService;
    }
    @Hidden
    @GetMapping("/reviews")
    public String getReview(@RequestParam String productName, @RequestParam String weather) {
        return reviewService.generateReview(productName, weather);
    }
    @Hidden
    @PostMapping("/generate")
    public String generateReview(@RequestBody ReviewRequest request) {
        return reviewService.generateReview(request.getProductName(), request.getWeather());
    }
    @Hidden
    @PostMapping("/generate/from-external")
    public String generateReviewFromExternalProduct(@RequestBody ExternalProductRequestWithWeather request) {
        ExternalProductResponse product = request.getProduct();
        String weather = request.getWeather();

        return reviewService.generateReviewFromExternalProduct(product, weather);
    }
    @Hidden
    @PostMapping("/generate/from-incoming")
    public String generateReviewFromIncomingProduct(@RequestBody IncomingProductRequest request, @RequestParam String weather) {
        return reviewService.generateReviewFromIncomingProduct(request, weather);
    }

}
