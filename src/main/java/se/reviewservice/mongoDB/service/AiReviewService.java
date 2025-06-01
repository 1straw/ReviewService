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
import se.reviewservice.openWeather.WeatherService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiReviewService {

    private final ClaudeClient claudeClient;
    private final ReviewRepository reviewRepository;
    private final WeatherService weatherService;

    public AiReviewService(ClaudeClient claudeClient, ReviewRepository reviewRepository, WeatherService weatherService) {
        this.claudeClient = claudeClient;
        this.reviewRepository = reviewRepository;
        this.weatherService = weatherService;
    }

    public String generateReview(String productName, String weather) {
        return generateReviewWithGroup(productName, weather, null, null);
    }
    public String generateReviewUsingLiveWeather(String productName, String lat, String lon, String groupId) {
        String weather = weatherService.getWeatherStockholm(lat, lon).block(); // Hämtar väder synkront
        return generateReviewWithGroup(productName, weather, groupId); // Ai-generering med vädret
    }

    // För bakåtkompatibilitet
    public String generateReviewWithGroup(String productName, String weather, String groupId) {
        return generateReviewWithGroup(productName, weather, groupId, null);
    }

    public String generateReviewWithGroup(String productName, String weather, String groupId, String productId) {
        // Hantera null eller tomt produktnamn
        String actualProductName = (productName == null || productName.trim().isEmpty()) ? "produkten" : productName;

        // Anpassa prompt baserat på grupp
        String promptTemplate = (groupId != null && groupId.equalsIgnoreCase("group5"))
                ? """
            Skriv exakt 5 realistiska kundrecensioner om "%s" baserat på att vädret är "%s".

            VIKTIGT: Recensionerna ska låta som äkta kundrecensioner. Nämn INTE "Grupp 5" eller "Produkt från Grupp 5" i texten. 
            Skriv som om det är en riktig produkt med ett riktigt namn.

            För varje recension, inkludera:
            - Namn på kunden
            - Betyg (1 till 5)
            - Själva recensionstexten

            Format:
            Namn: <namn>
            Betyg: <1–5>
            Recension: <text>

            Separera varje recension med två radbrytningar.
            """
                : """
            Skriv exakt 5 realistiska kundrecensioner om "%s" baserat på att vädret är "%s".

            För varje recension, inkludera:
            - Namn på kunden
            - Betyg (1 till 5)
            - Själva recensionstexten

            Format:
            Namn: <namn>
            Betyg: <1–5>
            Recension: <text>

            Separera varje recension med två radbrytningar.
            """;

        String prompt = String.format(promptTemplate, actualProductName, weather);
        String response = claudeClient.askClaude(prompt);

        List<Review> reviews = parseReviews(response, actualProductName, productId);
        reviews.forEach(reviewRepository::save);

        return "Claude generated and saved " + reviews.size() + " reviews.";
    }

    public String generateReviewFromExternalProduct(ExternalProductResponse product, String weather) {
        boolean isGroup5Product = product.getName() != null && product.getName().contains("Grupp 5");

        String prompt = isGroup5Product
                ? """
            Skriv exakt 5 realistiska kundrecensioner om produkten som beskrivs såhär: "%s".
            Priset är %.2f %s och vädret är "%s".
            
            VIKTIGT: Recensionerna ska låta som äkta kundrecensioner. Nämn INTE "Grupp 5" i texten.
            
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
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                weather
        )
                : """
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
        List<Review> reviews = parseReviews(response, product.getName(), product.getId());
        reviews.forEach(reviewRepository::save);

        return "Claude generated and saved " + reviews.size() + " reviews.";
    }

    public String generateReviewFromIncomingProduct(IncomingProductRequest product, String weather) {
        boolean isGroup5Product = product.getProductName() != null && product.getProductName().contains("Grupp 5");

        String promptTemplate = isGroup5Product
                ? """
            Skriv exakt 5 realistiska kundrecensioner.
            Kategorin är "%s", och den har dessa taggar: %s.
            Vädret är "%s".
            
            VIKTIGT: Recensionerna ska låta som äkta kundrecensioner. Nämn INTE "Grupp 5" i texten.
            
            För varje recension, inkludera:
            - Namn på kunden
            - Betyg (1 till 5)
            - Själva recensionstexten

            Format:
            Namn: <namn>
            Betyg: <1–5>
            Recension: <text>

            Separera varje recension med två radbrytningar.
            """
                : """
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
            """;

        String prompt = isGroup5Product
                ? String.format(
                promptTemplate,
                product.getCategory(),
                String.join(", ", product.getTags()),
                weather
        )
                : String.format(
                promptTemplate,
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

    private List<Review> parseReviews(String response, String productName, String actualProductId) {
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

                    // Skapa produkt för recensionen
                    Product product = new Product(
                            UUID.randomUUID().toString(),
                            productName,
                            "Automatiskt genererad produktbeskrivning",
                            BigDecimal.ZERO,
                            "default-group",
                            Map.of("category", "T-shirt")
                    );

                    // Skapa en ny recension
                    Review review = new Review(
                            UUID.randomUUID().toString(),
                            "demo company",
                            new Customer(name, ""),
                            product,
                            new ReviewDetails(rating, text),
                            Instant.now()
                    );

                    // Använd det riktiga productId:t när det är tillgängligt
                    review.setProductId(actualProductId != null && !actualProductId.trim().isEmpty()
                            ? actualProductId
                            : productName);

                    review.setReviewerName(name);
                    review.setRating(rating);
                    review.setComment(text);
                    review.setTitle("Review");
                    review.setDate(LocalDate.now());

                    return review;
                }).toList();
    }

    // För bakåtkompatibilitet
    private List<Review> parseReviews(String response, String productName) {
        return parseReviews(response, productName, null);
    }
}