package se.reviewservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Group6Review {
    private String text;
    private Integer rating;
    private String reviewerName;
    private LocalDate reviewDate;
}