package se.reviewservice.dto;


import lombok.Data;

@Data
public class ExternalProductRequestWithWeather {

    private ExternalProductResponse product;
    private String weather;
}
