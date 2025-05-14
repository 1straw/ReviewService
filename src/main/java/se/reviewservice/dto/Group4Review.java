package se.reviewservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Group4Review {
    private String reviewContent;
    private Integer rating;
    private String reviewerName;
    private LocalDate date;
}
