package se.reviewservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class Group4Response {
    // Formaterat betyg som stjärnor
    private String formattedRating;

    // Antal recensioner med "st" suffix
    private String totalReviews;

    // Lista med recensioner (bara text)
    private List<Group4Review> reviews;
}