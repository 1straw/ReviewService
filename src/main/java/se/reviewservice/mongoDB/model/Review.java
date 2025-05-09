package se.reviewservice.mongoDB.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String companyId;

    private Customer customer;
    private Product product;
    private ReviewDetails review;
    private Instant createdAt;
}
