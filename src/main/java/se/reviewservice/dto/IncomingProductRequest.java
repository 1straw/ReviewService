package se.reviewservice.dto;


import lombok.Data;

import java.util.List;

@Data
public class IncomingProductRequest {

    private String productId;
    private String productName;
    private String category;
    private List<String> tags;
}
