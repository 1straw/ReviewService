package se.reviewservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Group5Review {
    // Enbart de fält grupp 5 har specificerat att de behöver
    private Integer rating;            // Star rating (1-5)
    private String reviewContent;      // Customer Review
    private String reviewerName;       // Name (eller anonym)
    private String formattedDate;      // Date of review i format "Mar 2025"

    // Det formaterade svaret i exakt det format de efterfrågade
    private String formattedReview;    // "Review text" – Name, Date

    // Behåller original-ID för spårning om det behövs
    private String reviewId;
    private String productId;
}