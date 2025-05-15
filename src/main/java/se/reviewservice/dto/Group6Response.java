package se.reviewservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class Group6Response {
    private Double averageRating;
    private List<Group6Review> reviews;
}