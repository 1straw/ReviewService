package se.reviewservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import se.reviewservice.mongoDB.model.Customer;
import se.reviewservice.mongoDB.model.Review;
import se.reviewservice.mongoDB.model.ReviewDetails;
import se.reviewservice.mongoDB.repository.ReviewRepository;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(ReviewControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    public void setup() {
        reviewRepository.deleteAll();

        // Lägg till testdata för att säkerställa att controller-funktionen fungerar
        Customer customer = new Customer("John Doe", "john.doe@example.com");
        ReviewDetails reviewDetails = new ReviewDetails(5, "Great product!");
        Review review = new Review();
        review.setId("1");
        review.setCompanyId("company1");
        review.setCustomer(customer);
        review.setReview(reviewDetails);
        review.setCreatedAt(Instant.now());

        // Spara i databasen
        reviewRepository.save(review);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllReviews() throws Exception {
        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customer").isMap())  // Verifiera om customer finns och är ett objekt
                .andExpect(jsonPath("$[0].companyId").exists())  // Verifiera att companyId finns
                .andExpect(jsonPath("$[0].review").exists());  // Verifiera att review finns
    }

    @Test
    @WithMockUser
    public void testGetReviewById() throws Exception {
        String reviewId = "1";

        mockMvc.perform(get("/api/v1/reviews/{id}", reviewId)) // Skickar med reviewId i URL
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer").isMap());  // Verifiera att customer är ett objekt
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteReviewSuccess() throws Exception {
        // Förutsätt att du har en review som finns i databasen
        String reviewId = "1";

        mockMvc.perform(delete("/api/v1/reviews/{id}", reviewId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteReviewFail() throws Exception {

        String reviewId = "non-existing-id";


        mockMvc.perform(delete("/api/v1/reviews/{id}", reviewId))
                .andExpect(status().isNotFound());
    }
}



