package se.reviewservice.mongoDB.service;


import org.springframework.stereotype.Service;
import se.reviewservice.client.ClaudeClient;
import se.reviewservice.dto.ExternalProductResponse;
import se.reviewservice.dto.IncomingProductRequest;
import se.reviewservice.mongoDB.model.Customer;
import se.reviewservice.mongoDB.model.Product;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.model.ReviewDetails;
import se.reviewservice.mongoDB.repository.ReviewRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class AiReviewService {

    private final ClaudeClient claudeClient;
    private final ReviewRepository reviewRepository;


    public AiReviewService(ClaudeClient claudeClient, ReviewRepository reviewRepository) {
        this.claudeClient = claudeClient;
        this.reviewRepository = reviewRepository;
    }

    public String generateReview(String productName, String weather) {
        String prompt = """
        Skriv exakt 5 realistiska kundrecensioner om produkten "%s" baserat på att vädret är "%s".

        För varje recension, inkludera:
        - Namn på kunden
        - Betyg (1 till 5)
        - Själva recensionstexten

        Format:
        Namn: <namn>
        Betyg: <1–5>
        Recension: <text>

        Separera varje recension med två radbrytningar.
        """.formatted(productName, weather);

        String response = claudeClient.askClaude(prompt);

        List<Review> reviews = parseReviews(response, productName);
        reviews.forEach(reviewRepository::save);

        return "Claude generated and saved " + reviews.size() + " reviews.";

    }

    public String generateReviewFromExternalProduct(ExternalProductResponse product, String weather) {
        String prompt =  """
        Skriv exakt 5 realistiska kundrecensioner om produkten "%s" som beskrivs såhär: "%s".
        Priset är %.2f %s och vädret är "%s".
        
        För varje recension, inkludera:
        - Namn på kunden
        - Betyg (1 till 5)
        - Själva recensionstexten

        Format:
        Namn: <namn>
        Betyg: <1–5>
        Recension: <text>

        Separera varje recension med två radbrytningar.
    """.formatted(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                weather
        );

        String response = claudeClient.askClaude(prompt);
        List<Review> reviews = parseReviews(response, product.getName());
        reviews.forEach(reviewRepository::save);

        return "Claude generated and saved " + reviews.size() + " reviews.";
    }

    private List<Review> parseReviews(String response, String productName) {
        Pattern pattern = Pattern.compile(
                "Namn: (.+?)\\s*Betyg: (\\d)\\s*Recension: (.+?)(?=\\n{2}|$)",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(response);
        return matcher.results()
                .map(match -> {
                    String name = match.group(1).trim();
                    int rating = Integer.parseInt(match.group(2).trim());
                    String text = match.group(3).trim();

                    return new Review(
                            UUID.randomUUID().toString(),
                            "demo company", //ersätt sedan med faktiskt companyID
                            new Customer(name, ""),
                            new Product(productName, BigDecimal.ZERO, Map.of("category", "T-shirt")), //kategori ev. hårdkodad
                            new ReviewDetails(rating, text),
                            Instant.now()
                    );
                }).toList();
    }

    public String generateReviewFromIncomingProduct(IncomingProductRequest product, String weather) {
        String prompt = """
        Skriv exakt 5 realistiska kundrecensioner om produkten "%s".
        Kategorin är "%s", och den har dessa taggar: %s.
        Vädret är "%s".

        För varje recension, inkludera:
        - Namn på kunden
        - Betyg (1 till 5)
        - Själva recensionstexten

        Format:
        Namn: <namn>
        Betyg: <1–5>
        Recension: <text>

        Separera varje recension med två radbrytningar.
    """.formatted(
                product.getProductName(),
                product.getCategory(),
                String.join(", ", product.getTags()),
                weather
        );

        String response = claudeClient.askClaude(prompt);
        List<Review> reviews = parseReviews(response, product.getProductName());
        reviews.forEach(reviewRepository::save);

        return "Claude generated and saved " + reviews.size() + " reviews.";
    }

}
