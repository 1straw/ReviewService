package se.reviewservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalProductResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String image_url;
    private int stock_quantity;
    private boolean in_stock;
}
