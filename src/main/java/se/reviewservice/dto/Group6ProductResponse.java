package se.reviewservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class Group6ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("in_stock")
    private Boolean inStock;

    // Konstruktorer
    public Group6ProductResponse() {}

    public Group6ProductResponse(String id, String name, String description, BigDecimal price, String currency, String imageUrl, Integer stockQuantity, Boolean inStock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.imageUrl = imageUrl;
        this.stockQuantity = stockQuantity;
        this.inStock = inStock;
    }

    // Getters och Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }
}