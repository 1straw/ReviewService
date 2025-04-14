package se.reviewservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.reviewservice.model.Customer;

import java.util.List;

public interface ProductRepository extends JpaRepository <ProductRepository, String> {
    List<ProductRepository> findByCustomer(Customer customer);
    boolean existsByProductId(String productId); // Kollar om en produkt redan finnns

}
