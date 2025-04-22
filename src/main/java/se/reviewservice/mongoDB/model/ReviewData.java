package se.reviewservice.mongoDB.model;

import lombok.Data;

@Data
public class ReviewData {
    private int rating;
    private String text;
}
