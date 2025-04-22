package se.reviewservice.mongoDB.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.reviewservice.mongoDB.repository.ReviewRepository;
import se.reviewservice.mongoDB.model.Product;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.model.ReviewData;
import se.reviewservice.mongoDB.model.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository){
        this.reviewRepository = reviewRepository;
    }

    public List<Review> getAllReviews(){
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(String id) {
        return reviewRepository.findById(id);
    }

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }


    // Testa spara en produkt
    public void createSampleReview() {
        User user = new User();
        user.setName("Anna");
        user.setEmail("anna@example.com");

        Product product = new Product();
        product.setName("Nike Air Max");
        product.setPrice(BigDecimal.valueOf(1299.00));

        Map<String, Object> attributes = Map.of(
                "color", "White",
                "size", 42,
                "material", "Leather"
        );
        product.setAttributes(attributes);

        ReviewData reviewData = new ReviewData();
        reviewData.setRating(5);
        reviewData.setText("Jättesköna skor!");

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setReview(reviewData);
        review.setCreatedAt(Instant.now());

        reviewRepository.save(review);
    }


}
