package se.reviewservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.reviewservice.model.Customer;

import java.util.Optional;

public interface CustomerRepository  extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByApiKey(String apiKey);
    // För att hämta/hitta en kund antingen med användarnamn (för inloggning) eller med API-nyckel(autentisering via header).
}
