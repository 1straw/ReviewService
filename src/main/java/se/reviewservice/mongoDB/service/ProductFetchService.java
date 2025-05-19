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
        // Testa alla möjliga sätt att skicka API-nyckeln för grupp 5
        try {
            // Försök 1: X-API-Key header utan "Bearer"
            HttpHeaders headers1 = new HttpHeaders();
            headers1.set("X-API-Key", group5ApiKey);

            HttpEntity<String> entity1 = new HttpEntity<>(headers1);

            try {
                ResponseEntity<Group5ProductResponse[]> response1 = restTemplate.exchange(
                        endpoint,
                        HttpMethod.GET,
                        entity1,
                        Group5ProductResponse[].class
                );

                if (response1.getStatusCode() == HttpStatus.OK && response1.getBody() != null) {
                    Group5ProductResponse[] products = response1.getBody();
                    processGroup5Products(products, groupId);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 1 för grupp 5 misslyckades: " + e.getMessage());
            }

            // Försök 2: API-Key header (med stor bokstav)
            HttpHeaders headers2 = new HttpHeaders();
            headers2.set("API-Key", group5ApiKey);

            HttpEntity<String> entity2 = new HttpEntity<>(headers2);

            try {
                ResponseEntity<Group5ProductResponse[]> response2 = restTemplate.exchange(
                        endpoint,
                        HttpMethod.GET,
                        entity2,
                        Group5ProductResponse[].class
                );

                if (response2.getStatusCode() == HttpStatus.OK && response2.getBody() != null) {
                    Group5ProductResponse[] products = response2.getBody();
                    processGroup5Products(products, groupId);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 2 för grupp 5 misslyckades: " + e.getMessage());
            }

            // Försök 3: Authorization header med "Bearer"
            HttpHeaders headers3 = new HttpHeaders();
            headers3.set("Authorization", "Bearer " + group5ApiKey);

            HttpEntity<String> entity3 = new HttpEntity<>(headers3);

            try {
                ResponseEntity<Group5ProductResponse[]> response3 = restTemplate.exchange(
                        endpoint,
                        HttpMethod.GET,
                        entity3,
                        Group5ProductResponse[].class
                );

                if (response3.getStatusCode() == HttpStatus.OK && response3.getBody() != null) {
                    Group5ProductResponse[] products = response3.getBody();
                    processGroup5Products(products, groupId);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 3 för grupp 5 misslyckades: " + e.getMessage());
            }

            // Försök 4: api-key header (små bokstäver)
            HttpHeaders headers4 = new HttpHeaders();
            headers4.set("api-key", group5ApiKey);

            HttpEntity<String> entity4 = new HttpEntity<>(headers4);

            try {
                ResponseEntity<Group5ProductResponse[]> response4 = restTemplate.exchange(
                        endpoint,
                        HttpMethod.GET,
                        entity4,
                        Group5ProductResponse[].class
                );

                if (response4.getStatusCode() == HttpStatus.OK && response4.getBody() != null) {
                    Group5ProductResponse[] products = response4.getBody();
                    processGroup5Products(products, groupId);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 4 för grupp 5 misslyckades: " + e.getMessage());
            }

            // Försök 5: APIKey header (utan bindestreck)
            HttpHeaders headers5 = new HttpHeaders();
            headers5.set("APIKey", group5ApiKey);

            HttpEntity<String> entity5 = new HttpEntity<>(headers5);

            try {
                ResponseEntity<Group5ProductResponse[]> response5 = restTemplate.exchange(
                        endpoint,
                        HttpMethod.GET,
                        entity5,
                        Group5ProductResponse[].class
                );

                if (response5.getStatusCode() == HttpStatus.OK && response5.getBody() != null) {
                    Group5ProductResponse[] products = response5.getBody();
                    processGroup5Products(products, groupId);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 5 för grupp 5 misslyckades: " + e.getMessage());
            }

            // Försök 6: Som query parameter med olika parameternamn
            try {
                String urlWithApiKey1 = UriComponentsBuilder.fromHttpUrl(endpoint)
                        .queryParam("apiKey", group5ApiKey)
                        .build()
                        .toUriString();

                ResponseEntity<Group5ProductResponse[]> response6 = restTemplate.exchange(
                        urlWithApiKey1,
                        HttpMethod.GET,
                        null,
                        Group5ProductResponse[].class
                );

                if (response6.getStatusCode() == HttpStatus.OK && response6.getBody() != null) {
                    Group5ProductResponse[] products = response6.getBody();
                    processGroup5Products(products, groupId);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 6 för grupp 5 misslyckades: " + e.getMessage());
            }

            try {
                String urlWithApiKey2 = UriComponentsBuilder.fromHttpUrl(endpoint)
                        .queryParam("api-key", group5ApiKey)
                        .build()
                        .toUriString();

                ResponseEntity<Group5ProductResponse[]> response7 = restTemplate.exchange(
                        urlWithApiKey2,
                        HttpMethod.GET,
                        null,
                        Group5ProductResponse[].class
                );

                if (response7.getStatusCode() == HttpStatus.OK && response7.getBody() != null) {
                    Group5ProductResponse[] products = response7.getBody();
                    processGroup5Products(products, groupId);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 7 för grupp 5 misslyckades: " + e.getMessage());
            }

            try {
                String urlWithApiKey3 = UriComponentsBuilder.fromHttpUrl(endpoint)
                        .queryParam("key", group5ApiKey)
                        .build()
                        .toUriString();

                ResponseEntity<Group5ProductResponse[]> response8 = restTemplate.exchange(
                        urlWithApiKey3,
                        HttpMethod.GET,
                        null,
                        Group5ProductResponse[].class
                );

                if (response8.getStatusCode() == HttpStatus.OK && response8.getBody() != null) {
                    Group5ProductResponse[] products = response8.getBody();
                    processGroup5Products(products, groupId);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 8 för grupp 5 misslyckades: " + e.getMessage());
            }

            // Försök 9: X-API-KEY header (alla stora bokstäver)
            HttpHeaders headers9 = new HttpHeaders();
            headers9.set("X-API-KEY", group5ApiKey);
            // Lägg till standard Accept och Content-Type headers
            headers9.setContentType(MediaType.APPLICATION_JSON);
            headers9.set("Accept", "application/json");

            HttpEntity<String> entity9 = new HttpEntity<>(headers9);

            try {
                System.out.println("Försök 9: X-API-KEY (alla stora): " + group5ApiKey);
                ResponseEntity<Group5ProductResponse[]> response9 = restTemplate.exchange(
                        endpoint,
                        HttpMethod.GET,
                        entity9,
                        Group5ProductResponse[].class
                );

                if (response9.getStatusCode() == HttpStatus.OK && response9.getBody() != null) {
                    Group5ProductResponse[] products = response9.getBody();
                    processGroup5Products(products, groupId);
                    System.out.println("Försök 9 lyckades!");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 9 för grupp 5 misslyckades: " + e.getMessage());
            }

            // Försök 10: x-functions-key header (Azure Functions API Key)
            HttpHeaders headers10 = new HttpHeaders();
            headers10.set("x-functions-key", group5ApiKey);
            // Lägg till standard Accept och Content-Type headers
            headers10.setContentType(MediaType.APPLICATION_JSON);
            headers10.set("Accept", "application/json");

            HttpEntity<String> entity10 = new HttpEntity<>(headers10);

            try {
                System.out.println("Försök 10: x-functions-key (Azure Function): " + group5ApiKey);
                ResponseEntity<Group5ProductResponse[]> response10 = restTemplate.exchange(
                        endpoint,
                        HttpMethod.GET,
                        entity10,
                        Group5ProductResponse[].class
                );

                if (response10.getStatusCode() == HttpStatus.OK && response10.getBody() != null) {
                    Group5ProductResponse[] products = response10.getBody();
                    processGroup5Products(products, groupId);
                    System.out.println("Försök 10 lyckades!");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Försök 10 för grupp 5 misslyckades: " + e.getMessage());
            }

            System.err.println("Alla försök att komma åt grupp 5:s API misslyckades.");
            throw new RuntimeException("Kunde inte hitta rätt sätt att autentisera mot grupp 5:s API");

        } catch (Exception e) {
            throw e;
        }
    }

    private void processGroup4Products(ExternalProductResponse[] products, String groupId) {
        for (ExternalProductResponse extProduct : products) {
            Product product = mapGroup4ToProduct(extProduct, groupId);
            productRepository.save(product);

            if (!hasEnoughReviews(product.getId())) {
                String weather = "soligt"; // Ersätt med faktiskt väder
                aiReviewService.generateReviewFromExternalProduct(extProduct, weather);
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
                aiReviewService.generateReview(product.getName(), weather);
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
        product.setName(extProduct.getName());
        product.setDescription("Product from Group 5"); // Default beskrivning
        product.setPrice(BigDecimal.ZERO); // Default pris
        product.setGroupId(groupId);

        // Skapa ett Map för attributes med standard/tomma värden
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("source", "group5");

        product.setAttributes(attributes);

        return product;
    }

    private boolean hasEnoughReviews(String productId) {
        long count = reviewRepository.countByProductId(productId);
        return count >= 5;
    }

    // Manuell trigger
    public void manualFetchAllProducts() {
        fetchAllProductsFromGroups();
    }
}