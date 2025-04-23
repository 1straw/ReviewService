package se.reviewservice.h2db.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String text;
    private int rating;
    private LocalDate date;

    @ManyToOne
    private Product product;
}
