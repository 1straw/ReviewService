package se.reviewservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class Group4Response {
    private Double averageRating;
    private Integer totalReviews;
    private List<Group4Review> reviews;
}