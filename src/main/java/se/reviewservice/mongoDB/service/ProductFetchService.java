package se.reviewservice.mongoDB.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import se.reviewservice.dto.ExternalProductResponse;
import se.reviewservice.dto.Group5ProductResponse;
import se.reviewservice.mongoDB.model.Product;
import se.reviewservice.mongoDB.repository.ProductRepository;
import se.reviewservice.mongoDB.repository.ReviewRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductFetchService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AiReviewService aiReviewService;

    // Endpoints för alla grupper
    private final Map<String, String> groupEndpoints = Map.of(
            "group4", "https://merchstorecontainer.happycliff-80f98edc.swedencentral.azurecontainerapps.io/api/basic/products",
            "group5", "https://reviewapiv2250506.azurewebsites.net/api/products/simple",
            "group6", "URL_FÖR_GRUPP_6_HÄR"
    );

    // API-nycklar för grupper
    private final String group4ApiKey = "API_KEY"; // API-nyckel från grupp 4
    private final String group5ApiKey = "ItHurtWhenIPee"; // API-nyckel för grupp 5

    @Scheduled(fixedRate = 86400000) // Kör en gång per dag
    public void fetchAllProductsFromGroups() {
        for (Map.Entry<String, String> entry : groupEndpoints.entrySet()) {
            String groupId = entry.getKey();
            String endpoint = entry.getValue();

            fetchProductsForGroup(groupId, endpoint);
        }
    }

    private void fetchProductsForGroup(String groupId, String endpoint) {
        try {
            if (groupId.equals("group5")) {
                // Specialhantering för Grupp 5 med API-nyckel
                fetchGroup5Products(endpoint, groupId);
            } else if (groupId.equals("group4")) {
                // Specialhantering för Grupp 4 med API-nyckel
                fetchGroup4Products(endpoint, groupId);
            } else {
                // Standardhantering för övriga grupper utan API-nyckel
                fetchStandardProducts(endpoint, groupId);
            }
        } catch (Exception e) {
            System.err.println("Error fetching products for group " + groupId + ": " + e.getMessage());
        }
    }

    private void fetchGroup4Products(String endpoint, String groupId) {
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;

        while (retryCount < maxRetries && !success) {
            try {
                // Använd endast den metod som tidigare fungerade
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-API-Key", group4ApiKey);

                // Lägg till standard Accept och Content-Type headers
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Accept", "application/json");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                System.out.println("Attempting to fetch products from group4, attempt " + (retryCount + 1));
                System.out.println("URL: " + endpoint);
                System.out.println("Headers: " + headers);

                ResponseEntity<ExternalProductResponse[]> response = restTemplate.exchange(
                        endpoint,
                        HttpMethod.GET,
                        entity,
                        ExternalProductResponse[].class
                );

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    ExternalProductResponse[] products = response.getBody();
                    processGroup4Products(products, groupId);
                    success = true;
                    System.out.println("Successfully fetched " + products.length + " products from group4");
                }
            } catch (HttpServerErrorException.InternalServerError e) {
                System.err.println("Server error from group4, retry attempt " + (retryCount + 1) + ": " + e.getMessage());
                // Server Error (500) - vänta lite och försök igen
                try {
                    Thread.sleep(1000 * (retryCount + 1)); // Exponentiell backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                System.err.println("Error fetching from group4, retry attempt " + (retryCount + 1) + ": " + e.getMessage());
                // Andra fel - vänta lite och försök igen
                try {
                    Thread.sleep(1000 * (retryCount + 1)); // Exponentiell backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            retryCount++;
        }

        if (!success) {
            throw new RuntimeException("Failed to fetch products from group4 after " + maxRetries + " attempts");
        }
    }

    private void fetchStandardProducts(String endpoint, String groupId) {
        // Kontrollera om det är platshållaren för grupp 6
        if (groupId.equals("group6") && endpoint.equals("URL_FÖR_GRUPP_6_HÄR")) {
            System.err.println("Ingen giltig URL tillgänglig för grupp 6. Skippar...");
            throw new IllegalArgumentException("Ingen giltig URL för grupp 6");
        }

        ResponseEntity<?> response;

        if (groupId.equals("group6")) {
            // Grupp 6 format - implementera när du har information
            try {
                response = restTemplate.getForEntity(endpoint, Object[].class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Object[] products = (Object[]) response.getBody();
                    System.out.println("Fetched " + products.length + " products for group " + groupId);
                    // Implementera processGroup6Products när du har information
                }
            } catch (Exception e) {
                System.err.println("Error fetching products for group " + groupId + ": " + e.getMessage());
                throw e;
            }
        }
    }

    private void fetchGroup5Products(String endpoint, String groupId) {
        try {
            // Använd bara det format som vi vet fungerar
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-functions-key", group5ApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            System.out.println("Försöker hämta produkter från grupp 5 med x-functions-key: " + group5ApiKey);
            ResponseEntity<Group5ProductResponse[]> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    Group5ProductResponse[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Group5ProductResponse[] products = response.getBody();

                // Logga alla produkter för att se vad API:et faktiskt returnerar
                for (Group5ProductResponse product : products) {
                    System.out.println("Produkt från grupp 5 API - ID: " + product.getId() +
                            ", Namn: " + product.getName());
                }

                processGroup5Products(products, groupId);
                System.out.println("Lyckades hämta " + products.length + " produkter från grupp 5");
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException("Kunde inte ansluta till grupp 5:s API: " + e.getMessage(), e);
        }
    }

    private void processGroup4Products(ExternalProductResponse[] products, String groupId) {
        for (ExternalProductResponse extProduct : products) {
            Product product = mapGroup4ToProduct(extProduct, groupId);
            productRepository.save(product);

            if (!hasEnoughReviews(product.getId())) {
                String weather = "soligt"; // Ersätt med faktiskt väder
                aiReviewService.generateReviewWithGroup(product.getName(), weather, groupId);
            }
        }

        System.out.println("Processed " + products.length + " products for group " + groupId);
    }

    private void processGroup5Products(Group5ProductResponse[] products, String groupId) {
        for (Group5ProductResponse extProduct : products) {
            Product product = mapGroup5ToProduct(extProduct, groupId);
            productRepository.save(product);

            if (!hasEnoughReviews(product.getId())) {
                String weather = "soligt"; // Ersätt med faktiskt väder
                aiReviewService.generateReviewWithGroup(product.getName(), weather, groupId);
            }
        }

        System.out.println("Processed " + products.length + " products for group " + groupId);
    }

    private Product mapGroup4ToProduct(ExternalProductResponse extProduct, String groupId) {
        Product product = new Product();
        product.setId(extProduct.getId());
        product.setName(extProduct.getName());
        product.setDescription(extProduct.getDescription());
        product.setPrice(extProduct.getPrice());
        product.setGroupId(groupId);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("currency", extProduct.getCurrency());
        attributes.put("image_url", extProduct.getImage_url());
        attributes.put("stock_quantity", extProduct.getStock_quantity());
        attributes.put("in_stock", extProduct.isIn_stock());

        product.setAttributes(attributes);

        return product;
    }

    private Product mapGroup5ToProduct(Group5ProductResponse extProduct, String groupId) {
        Product product = new Product();
        product.setId(extProduct.getId());

        // FIX: Sätt ett standardnamn om inget namn finns
        if (extProduct.getName() == null || extProduct.getName().trim().isEmpty()) {
            product.setName("Produkt från Grupp 5 - " + extProduct.getId());
        } else {
            product.setName(extProduct.getName());
        }

        product.setDescription("Product from Group 5"); // Default beskrivning
        product.setPrice(BigDecimal.ZERO); // Default pris
        product.setGroupId(groupId);

        // Skapa ett Map för attributes med standard/tomma värden
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("source", "group5");
        attributes.put("original_id", extProduct.getId()); // Spara original-ID som ett attribut

        product.setAttributes(attributes);

        return product;
    }

    private boolean hasEnoughReviews(String productId) {
        long count = reviewRepository.countByProductId(productId);
        return count > 0;  // Ändrat från "count >= 5" till "count > 0"
    }

    // Manuell trigger
    public void manualFetchAllProducts() {
        fetchAllProductsFromGroups();
    }
}