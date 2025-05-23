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

    public AiReviewService(ClaudeClient claudeClient, ReviewRepository reviewRepository) {
        this.claudeClient = claudeClient;
        this.reviewRepository = reviewRepository;
    }

    public String generateReview(String productName, String weather) {
        return generateReviewWithGroup(productName, weather, null, null);
    }

    // BEHÅLL GAMLA METODEN för bakåtkompatibilitet
    public String generateReviewWithGroup(String productName, String weather, String groupId) {
        return generateReviewWithGroup(productName, weather, groupId, null);
    }

    // NY METOD: med productId parameter
    public String generateReviewWithGroup(String productName, String weather, String groupId, String productId) {
        // Hantera null eller tomt produktnamn
        String actualProductName = productName;
        if (productName == null || productName.trim().isEmpty()) {
            if (groupId != null && groupId.equalsIgnoreCase("group5")) {
                // För grupp 5, använd en generisk produktbeskrivning utan att nämna "Grupp 5"
                actualProductName = "produkten";
            } else {
                actualProductName = "produkten";
            }
            System.out.println("Använder generiskt produktnamn eftersom inget namn finns");
        }

        // Anpassa prompt baserat på grupp
        String promptTemplate;
        if (groupId != null && groupId.equalsIgnoreCase("group5")) {
            // För grupp 5, se till att inte nämna "Grupp 5" i recensionen
            promptTemplate = """
            Skriv exakt 5 realistiska kundrecensioner om "%s" baserat på att vädret är "%s".

            VIKTIGT: Recensionerna ska låta som äkta kundrecensioner. Nämn INTE "Grupp 5" eller "Produkt från Grupp 5" i texten. 
            Skriv som om det är en riktig produkt med ett riktigt namn. Om produkten heter "Produkt från Grupp 5", referera till den 
            bara som "produkten" eller ett lämpligt produktnamn.

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
        } else {
            // För andra grupper, använd normal prompt
            promptTemplate = """
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
        }

        String prompt = String.format(promptTemplate, actualProductName, weather);
        String response = claudeClient.askClaude(prompt);

        // UPPDATERING: Skicka med productId till parseReviews
        List<Review> reviews = parseReviews(response, actualProductName, productId);
        reviews.forEach(reviewRepository::save);

        return "Claude generated and saved " + reviews.size() + " reviews.";
    }

    public String generateReviewFromExternalProduct(ExternalProductResponse product, String weather) {
        String prompt;
        if (product.getName() != null && product.getName().contains("Grupp 5")) {
            // Om produktnamnet innehåller "Grupp 5", använd en anpassad prompt
            prompt = """
            Skriv exakt 5 realistiska kundrecensioner om produkten som beskrivs såhär: "%s".
            Priset är %.2f %s och vädret är "%s".
            
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
            """.formatted(
                    product.getDescription(),
                    product.getPrice(),
                    product.getCurrency(),
                    weather
            );
        } else {
            // Vanlig prompt för övriga produkter
            prompt = """
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
        }

        String response = claudeClient.askClaude(prompt);
        List<Review> reviews = parseReviews(response, product.getName(), product.getId());
        reviews.forEach(reviewRepository::save);

        return "Claude generated and saved " + reviews.size() + " reviews.";
    }

    // UPPDATERAD METOD: Lägg till productId parameter
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
                    String tempProductId = UUID.randomUUID().toString();

                    Product product = new Product(
                            tempProductId,               // id
                            productName,                 // name
                            "Automatiskt genererad produktbeskrivning", // description
                            BigDecimal.ZERO,             // price
                            "default-group",             // groupId
                            Map.of("category", "T-shirt") // attributes
                    );

                    // Skapa en ny recension med den befintliga konstruktorn
                    Review review = new Review(
                            UUID.randomUUID().toString(), // id
                            "demo company",              // companyId
                            new Customer(name, ""),      // customer
                            product,                     // product
                            new ReviewDetails(rating, text), // reviewDetails
                            Instant.now()                // createdAt
                    );

                    // VIKTIGT: Använd det riktiga productId:t från databasen, inte produktnamnet
                    if (actualProductId != null && !actualProductId.trim().isEmpty()) {
                        review.setProductId(actualProductId);
                        System.out.println("Sätter productId till: " + actualProductId);
                    } else {
                        review.setProductId(productName);
                        System.out.println("Använder produktnamn som productId: " + productName);
                    }

                    // Sätt alla relevanta fält explicit
                    review.setReviewerName(name);
                    review.setRating(rating);
                    review.setComment(text);
                    review.setTitle("Review");
                    review.setDate(LocalDate.now());

                    return review;
                }).toList();
    }

    // ÖVERLAGD METOD för bakåtkompatibilitet
    private List<Review> parseReviews(String response, String productName) {
        return parseReviews(response, productName, null);
    }

    public String generateReviewFromIncomingProduct(IncomingProductRequest product, String weather) {
        String promptTemplate;
        if (product.getProductName() != null && product.getProductName().contains("Grupp 5")) {
            // Om produktnamnet innehåller "Grupp 5", använd en anpassad prompt
            promptTemplate = """
            Skriv exakt 5 realistiska kundrecensioner.
            Kategorin är "%s", och den har dessa taggar: %s.
            Vädret är "%s".
            
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
            """;
        } else {
            // Vanlig prompt för övriga produkter
            promptTemplate = """
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
        }

        String prompt;
        if (product.getProductName() != null && product.getProductName().contains("Grupp 5")) {
            prompt = String.format(
                    promptTemplate,
                    product.getCategory(),
                    String.join(", ", product.getTags()),
                    weather
            );
        } else {
            prompt = String.format(
                    promptTemplate,
                    product.getProductName(),
                    product.getCategory(),
                    String.join(", ", product.getTags()),
                    weather
            );
        }

        String response = claudeClient.askClaude(prompt);
        List<Review> reviews = parseReviews(response, product.getProductName());
        reviews.forEach(reviewRepository::save);

        return "Claude generated and saved " + reviews.size() + " reviews.";
    }
}