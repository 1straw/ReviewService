package se.reviewservice.mongoDB.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class Product {
    private String name;
    private BigDecimal price;


    private Map<String, Object> attributes;
}
