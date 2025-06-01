package se.reviewservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import se.reviewservice.dto.ExternalProductResponse;
import se.reviewservice.dto.Group5ProductResponse;
import se.reviewservice.dto.Group6ProductResponse;
import se.reviewservice.model.Product;
import se.reviewservice.repository.ProductRepository;
import se.reviewservice.repository.ReviewRepository;

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
            "group6", "https://fanta-stick-six.azurewebsites.net/api/basic/products"
    );

    // API-nycklar för grupper
    private final String group4ApiKey = "API_KEY";
    private final String group5ApiKey = "ItHurtWhenIPee";
    private final String group6ApiKey = "Merchstore_ApiKey";

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
                fetchGroup5Products(endpoint, groupId);
            } else if (groupId.equals("group4")) {
                fetchGroup4Products(endpoint, groupId);
            } else if (groupId.equals("group6")) {
                fetchGroup6Products(endpoint, groupId);
            }
        } catch (Exception e) {
            // Logga fel men fortsätt exekveringen
        }
    }

    private void fetchGroup4Products(String endpoint, String groupId) {
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;

        while (retryCount < maxRetries && !success) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-API-Key", group4ApiKey);
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Accept", "application/json");

                HttpEntity<String> entity = new HttpEntity<>(headers);

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
                }
            } catch (HttpServerErrorException.InternalServerError e) {
                try {
                    Thread.sleep(1000 * (retryCount + 1)); // Exponentiell backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
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

    private void fetchGroup5Products(String endpoint, String groupId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-functions-key", group5ApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Group5ProductResponse[]> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    Group5ProductResponse[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Group5ProductResponse[] products = response.getBody();
                processGroup5Products(products, groupId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to group5 API", e);
        }
    }

    private void fetchGroup6Products(String endpoint, String groupId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", group6ApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Group6ProductResponse[]> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    Group6ProductResponse[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Group6ProductResponse[] products = response.getBody();
                processGroup6Products(products, groupId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to group6 API", e);
        }
    }

    private void processGroup4Products(ExternalProductResponse[] products, String groupId) {
        for (ExternalProductResponse extProduct : products) {
            Product product = mapGroup4ToProduct(extProduct, groupId);
            productRepository.save(product);

            if (!hasEnoughReviews(product.getId())) {
                String weather = "soligt";
                aiReviewService.generateReviewWithGroup(product.getName(), weather, groupId, product.getId());
            }
        }
    }

    private void processGroup5Products(Group5ProductResponse[] products, String groupId) {
        for (Group5ProductResponse extProduct : products) {
            Product product = mapGroup5ToProduct(extProduct, groupId);
            productRepository.save(product);

            if (!hasEnoughReviews(product.getId())) {
                String weather = "soligt";
                aiReviewService.generateReviewWithGroup(product.getName(), weather, groupId, product.getId());
            }
        }
    }

    private void processGroup6Products(Group6ProductResponse[] products, String groupId) {
        for (Group6ProductResponse extProduct : products) {
            Product product = mapGroup6ToProduct(extProduct, groupId);
            productRepository.save(product);

            if (!hasEnoughReviews(product.getId())) {
                String weather = "soligt";
                aiReviewService.generateReviewWithGroup(product.getName(), weather, groupId, product.getId());
            }
        }
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
        product.setDescription("Product from Group 5");
        product.setPrice(BigDecimal.ZERO);
        product.setGroupId(groupId);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("source", "group5");
        attributes.put("original_id", extProduct.getId());
        product.setAttributes(attributes);

        return product;
    }

    private Product mapGroup6ToProduct(Group6ProductResponse extProduct, String groupId) {
        Product product = new Product();
        product.setId(extProduct.getId());
        product.setName(extProduct.getName());
        product.setDescription(extProduct.getDescription());
        product.setPrice(extProduct.getPrice());
        product.setGroupId(groupId);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("currency", extProduct.getCurrency());
        attributes.put("image_url", extProduct.getImageUrl());
        attributes.put("stock_quantity", extProduct.getStockQuantity());
        attributes.put("in_stock", extProduct.getInStock());

        product.setAttributes(attributes);

        return product;
    }

    private boolean hasEnoughReviews(String productId) {
        long count = reviewRepository.countByProductId(productId);
        return count > 0;
    }

    // Manuell trigger
    public void manualFetchAllProducts() {
        fetchAllProductsFromGroups();
    }
}