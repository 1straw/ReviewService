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
    @GetMapping("/reviews")
    public String getReview(@RequestParam String productName, @RequestParam String weather) {
        return reviewService.generateReview(productName, weather);
    }
    @PostMapping("/generate")
    public String generateReview(@RequestBody ReviewRequest request) {
        return reviewService.generateReview(request.getProductName(), request.getWeather());
    }
    @PostMapping("/generate/from-external")
    public String generateReviewFromExternalProduct(@RequestBody ExternalProductRequestWithWeather request) {
        ExternalProductResponse product = request.getProduct();
        String weather = request.getWeather();

        return reviewService.generateReviewFromExternalProduct(product, weather);
    }
    @PostMapping("/generate/from-incoming")
    public String generateReviewFromIncomingProduct(@RequestBody IncomingProductRequest request, @RequestParam String weather) {
        return reviewService.generateReviewFromIncomingProduct(request, weather);
    }
    @PostMapping("/generate/live")
    public String generateReviewWithLiveWeather(
            @RequestParam String productName,
            @RequestParam(required = false) String lat,
            @RequestParam(required = false) String lon,
            @RequestParam(required = false) String groupId
    ) {
        if(lat == null || lon == null) {
            lat = "59.3251172"; // Stockholm default
            lon = "18.0710935";
        }
        return reviewService.generateReviewUsingLiveWeather(productName, lat, lon, groupId);
    }

}
