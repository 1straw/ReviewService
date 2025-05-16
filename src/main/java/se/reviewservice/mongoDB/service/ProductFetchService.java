package se.reviewservice.mongoDB.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import se.reviewservice.dto.ExternalProductResponse;
import se.reviewservice.dto.Group5ProductResponse;
import se.reviewservice.mongoDB.model.Product;
import se.reviewservice.mongoDB.repository.ProductRepository;
import se.reviewservice.mongoDB.repository.ReviewRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    // Om vi ska använda mockad data eller inte
    @Value("${use.mocked.data:false}")
    private boolean useMockedData;

    // Endpoints för alla grupper
    private final Map<String, String> groupEndpoints = Map.of(
            "group4", "https://merchstorecontainer.happycliff-80f98edc.swedencentral.azurecontainerapps.io/api/basic/products",
            "group5", "https://reviewapiv2250506.azurewebsites.net/api/products/simple",
            "group6", "URL_FÖR_GRUPP_6_HÄR"
    );

    // API-nyckel bara för Grupp 5
    private final String group5ApiKey = "ItHurtWhenIPee";

    @Scheduled(fixedRate = 86400000) // Kör en gång per dag
    public void fetchAllProductsFromGroups() {
        if (useMockedData) {
            System.out.println("Using mocked data as configured by application properties.");
            for (String groupId : groupEndpoints.keySet()) {
                fetchMockedProducts(groupId);
            }
            return;
        }

        for (Map.Entry<String, String> entry : groupEndpoints.entrySet()) {
            String groupId = entry.getKey();
            String endpoint = entry.getValue();

            fetchProductsForGroup(groupId, endpoint);
        }
    }

    private void fetchProductsForGroup(String groupId, String endpoint) {
        try {
            if (useMockedData) {
                // Om konfigurerad att använda mockade data, använd det direkt
                fetchMockedProducts(groupId);
                return;
            }

            if (groupId.equals("group5")) {
                // Specialhantering för Grupp 5 med API-nyckel
                fetchGroup5Products(endpoint, groupId);
            } else {
                // Standardhantering för övriga grupper utan API-nyckel
                fetchStandardProducts(endpoint, groupId);
            }
        } catch (Exception e) {
            System.err.println("Error fetching real products for group " + groupId + ": " + e.getMessage());
            System.out.println("Falling back to mocked products for group " + groupId);

            // Om det misslyckas, använd mockade produkter
            fetchMockedProducts(groupId);
        }
    }

    private void fetchStandardProducts(String endpoint, String groupId) {
        ResponseEntity<?> response;

        if (groupId.equals("group4")) {
            // Grupp 4 format
            response = restTemplate.getForEntity(endpoint, ExternalProductResponse[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ExternalProductResponse[] products = (ExternalProductResponse[]) response.getBody();
                processGroup4Products(products, groupId);
            }
        } else if (groupId.equals("group6")) {
            // Grupp 6 format - implementera när du har information
            // Tillfällig implementation:
            response = restTemplate.getForEntity(endpoint, Object[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object[] products = (Object[]) response.getBody();
                System.out.println("Fetched " + products.length + " products for group " + groupId);
                // Implementera processGroup6Products när du har information
            }
        }
    }

    private void fetchGroup5Products(String endpoint, String groupId) {
        // Prova med query parameter
        String urlWithApiKey = UriComponentsBuilder.fromHttpUrl(endpoint)
                .queryParam("apiKey", group5ApiKey)
                .build()
                .toUriString();

        // Anropa API med query parameter
        ResponseEntity<Group5ProductResponse[]> response =
                restTemplate.exchange(
                        urlWithApiKey,
                        HttpMethod.GET,
                        null, // Ingen entity behövs när vi använder query parameter
                        Group5ProductResponse[].class
                );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Group5ProductResponse[] products = response.getBody();
            processGroup5Products(products, groupId);
        }
    }

    private void fetchMockedProducts(String groupId) {
        System.out.println("Fetching mocked products for group " + groupId);

        List<Product> mockedProducts = new ArrayList<>();

        if (groupId.equals("group4")) {
            // Skapa mockade produkter för grupp 4
            for (int i = 1; i <= 5; i++) {
                String productId = "mock-g4-" + i;

                // Konvertera först till ExternalProductResponse-objekt för att testa mappning
                ExternalProductResponse extProduct = new ExternalProductResponse();
                extProduct.setId(productId);
                extProduct.setName("Mockad Produkt G4-" + i);
                extProduct.setDescription("Detta är en mockad produkt från grupp 4 för testning.");
                extProduct.setPrice(new BigDecimal("" + (i * 100)));
                extProduct.setCurrency("SEK");
                extProduct.setImage_url("https://example.com/images/mock-product-" + i + ".jpg");
                extProduct.setStock_quantity(50);
                extProduct.setIn_stock(true);

                // Spara först Product-objektet
                Product product = mapGroup4ToProduct(extProduct, groupId);
                productRepository.save(product);
                mockedProducts.add(product);

                // Sedan generera recensioner om det behövs
                if (!hasEnoughReviews(product.getId())) {
                    String weather = "soligt"; // Ersätt med faktiskt väder
                    aiReviewService.generateReviewFromExternalProduct(extProduct, weather);
                }
            }
        } else if (groupId.equals("group5")) {
            // Skapa mockade produkter för grupp 5
            for (int i = 1; i <= 5; i++) {
                String productId = "mock-g5-" + i;

                // Skapa Group5ProductResponse för testning
                Group5ProductResponse extProduct = new Group5ProductResponse();
                extProduct.setId(productId);
                extProduct.setName("Mockad Produkt G5-" + i);

                // Spara produkt
                Product product = mapGroup5ToProduct(extProduct, groupId);
                productRepository.save(product);
                mockedProducts.add(product);

                // Generera recensioner
                if (!hasEnoughReviews(product.getId())) {
                    String weather = "soligt";
                    aiReviewService.generateReview(product.getName(), weather);
                }
            }
        } else if (groupId.equals("group6")) {
            // Skapa mockade produkter för grupp 6 - anpassas efter din Group6ProductResponse
            for (int i = 1; i <= 5; i++) {
                Product product = new Product();
                product.setId("mock-g6-" + i);
                product.setName("Mockad Produkt G6-" + i);
                product.setDescription("Detta är en mockad produkt från grupp 6 för testning.");
                product.setPrice(new BigDecimal("" + (i * 150)));
                product.setGroupId(groupId);

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("category", "Mockad Kategori");
                attributes.put("color", "Blå");
                product.setAttributes(attributes);

                productRepository.save(product);
                mockedProducts.add(product);

                if (!hasEnoughReviews(product.getId())) {
                    String weather = "soligt";
                    aiReviewService.generateReview(product.getName(), weather);
                }
            }
        }

        System.out.println("Created " + mockedProducts.size() + " mocked products for group " + groupId);
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