package se.reviewservice.mongoDB.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {
    private String name;
    private BigDecimal price;


    private Map<String, Object> attributes;
}
