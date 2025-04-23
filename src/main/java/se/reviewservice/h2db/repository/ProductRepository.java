package se.reviewservice.h2db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.reviewservice.h2db.model.Customer;
import se.reviewservice.h2db.model.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository <Product, String> {
    List<ProductRepository> findByCustomer(Customer customer);
    boolean existsByProductId(String productId); // Kollar om en produkt redan finnns
}
