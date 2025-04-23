package se.reviewservice.h2db.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Product {
    @Id
    private String productId;
    private String name;
    private String category;

    @ManyToOne
    private Customer customer;

}
