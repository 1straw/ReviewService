package se.reviewservice.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.reviewservice.dto.ExternalProductRequestWithWeather;
import se.reviewservice.dto.ExternalProductResponse;
import se.reviewservice.dto.IncomingProductRequest;
import se.reviewservice.dto.ReviewRequest;
import se.reviewservice.service.AiReviewService;

@Hidden
@RestController
@RequestMapping("/api/v1/ai")
public class AiReviewController {

    private final AiReviewService reviewService;
    private static final Logger logger = LoggerFactory.getLogger(AiReviewController.class);

    public AiReviewController(AiReviewService reviewService) {
        this.reviewService = reviewService;
    }
    @GetMapping("/reviews")
    public ResponseEntity<String> getReview(@RequestParam String productName, @RequestParam String weather) {
        try {
            logger.info("GET /reviews - product: {}, weather: {}", productName, weather);
            return ResponseEntity.ok(reviewService.generateReview(productName, weather));
        } catch (Exception e) {
            logger.error("Fel vid hämtning av recensioner", e);
            return ResponseEntity.status(500).body("Något gick fel när recensionen skulle hämtas.");
        }
    }
    @PostMapping("/generate")
    public ResponseEntity<String> generateReview(@RequestBody ReviewRequest request) {
        try {
            logger.info("POST /generate - product: {}", request.getProductName());
            String result = reviewService.generateReview(request.getProductName(), request.getWeather());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Fel vid generering av recension", e);
            return ResponseEntity.status(500).body("Något gick fel när recensionen skulle genereras.");
        }
    }
    @PostMapping("/generate/from-external")
    public ResponseEntity<String> generateReviewFromExternalProduct(@RequestBody ExternalProductRequestWithWeather request) {
        try {
            ExternalProductResponse product = request.getProduct();
            logger.info("POST /generate/from-external - external product: {}", product.getName());
            String result = reviewService.generateReviewFromExternalProduct(product, request.getWeather());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Fel vid generering från extern produkt", e);
            return ResponseEntity.status(500).body("Fel vid generering från extern produkt.");
        }
    }
    @PostMapping("/generate/from-incoming")
    public ResponseEntity<String> generateReviewFromIncomingProduct(@RequestBody IncomingProductRequest request, @RequestParam String weather) {
        try {
            logger.info("POST /generate/from-incoming - product: {}", request.getProductName());
            String result = reviewService.generateReviewFromIncomingProduct(request, weather);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Fel vid generering från inkommande produkt", e);
            return ResponseEntity.status(500).body("Fel vid generering från inkommande produkt.");
        }
    }
    @PostMapping("/generate/live")
    public ResponseEntity<String> generateReviewWithLiveWeather(
            @RequestParam String productName,
            @RequestParam(required = false) String lat,
            @RequestParam(required = false) String lon,
            @RequestParam(required = false) String groupId
    ) {
        try {
            if (lat == null || lon == null) {
                lat = "59.3251172"; // Stockholm default
                lon = "18.0710935";
            }
            logger.info("POST /generate/live - product: {}, lat: {}, lon: {}, groupId: {}", productName, lat, lon, groupId);
            String result = reviewService.generateReviewUsingLiveWeather(productName, lat, lon, groupId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Fel vid generering med väderdata", e);
            return ResponseEntity.status(500).body("Fel vid generering med väderdata.");
        }
    }
}
