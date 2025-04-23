package se.reviewservice.h2db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.reviewservice.h2db.model.Product;
import se.reviewservice.h2db.model.Review;

import java.time.LocalDate;
import java.util.List;

public interface H2ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);

    //Recensioner från de senaste två månaderna
    List<Review> findByProductAndDateAfter(Product product, LocalDate date);

    //Hämtar de 10 senaste recensionerna
    List<Review> findTop10ByProductOrderByDateDesc(Product product);

    //Hämta alla recensioner sorterade bakåt i tiden
    List<Review> findByProductOrderByDateDesc(Product product);


}
