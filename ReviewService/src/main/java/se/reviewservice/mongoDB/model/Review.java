package se.reviewservice.mongoDB.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;

    private User user;
    private Product product;
    private ReviewData review;
    private Instant createdAt;
}
