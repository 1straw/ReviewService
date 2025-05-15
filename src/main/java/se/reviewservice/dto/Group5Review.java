package se.reviewservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Group5Review {
    private String reviewId;
    private String productId;
    private String reviewerName;
    private String reviewTitle;
    private String reviewContent;
    private Integer rating;
    private LocalDate creationDate;
}