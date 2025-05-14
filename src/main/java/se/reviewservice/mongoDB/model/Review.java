package se.reviewservice.mongoDB.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@NoArgsConstructor
@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String companyId;
    private String productId;

    private Customer customer;
    private Product product;
    private ReviewDetails review;
    private Instant createdAt;

    private String reviewerName;
    private String title;
    private String comment;
    private int rating;
    private LocalDate date;

    // Anpassad konstruktor som matchar den gamla signaturen
    public Review(String id, String companyId, Customer customer, Product product, ReviewDetails review, Instant createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.customer = customer;
        this.product = product;
        this.review = review;
        this.createdAt = createdAt;

        // Sätt ytterligare fält från de inkommande värdena
        if (customer != null) {
            this.reviewerName = customer.getName();
        }

        if (review != null) {
            this.comment = review.getText();
            this.rating = review.getRating();
        }

        // Använd produktnamn som productId om tillgängligt, annars company
        if (product != null && product.getName() != null) {
            this.productId = product.getName();
        } else {
            this.productId = companyId;
        }

        if (createdAt != null) {
            this.date = createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
    }

    // Full konstruktor för alla fält
    public Review(String id, String companyId, String productId, Customer customer, Product product, ReviewDetails review,
                  Instant createdAt, String reviewerName, String title, String comment, int rating, LocalDate date) {
        this.id = id;
        this.companyId = companyId;
        this.productId = productId;
        this.customer = customer;
        this.product = product;
        this.review = review;
        this.createdAt = createdAt;
        this.reviewerName = reviewerName;
        this.title = title;
        this.comment = comment;
        this.rating = rating;
        this.date = date;
    }

    // Hjälpmetoder för att få konsekvent data oavsett källfält
    public String getReviewerName() {
        if (reviewerName != null) {
            return reviewerName;
        } else if (customer != null && customer.getName() != null) {
            return customer.getName();
        }
        return "Anonymous";
    }

    public String getTitle() {
        return title != null ? title : "Review";
    }

    public String getComment() {
        if (comment != null) {
            return comment;
        } else if (review != null && review.getText() != null) {
            return review.getText();
        }
        return "";
    }

    public int getRating() {
        if (rating > 0) {
            return rating;
        } else if (review != null) {
            return review.getRating();
        }
        return 0;
    }

    public LocalDate getDate() {
        if (date != null) {
            return date;
        } else if (createdAt != null) {
            return createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        return LocalDate.now();
    }
}