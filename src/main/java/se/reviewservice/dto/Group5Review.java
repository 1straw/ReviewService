package se.reviewservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Group5Review {
    // ENDAST de 7 fält som Grupp 5 har specificerat
    private String reviewId;           // Review id
    private String productId;          // ProductID
    private String reviewerName;       // ReviewerName
    private String reviewTitle;        // ReviewTitle
    private String reviewContent;      // ReviewContent (text from reviewer)
    private Integer rating;            // Rating (1-5)
    private String creationDate;       // CreationDate
}