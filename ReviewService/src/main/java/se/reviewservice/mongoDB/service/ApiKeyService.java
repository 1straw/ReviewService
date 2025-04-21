package se.reviewservice.mongoDB.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.reviewservice.mongoDB.model.AuthUser;
import se.reviewservice.mongoDB.repository.AuthUserRepository;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ApiKeyService {

    @Autowired
    private AuthUserRepository authUserRepository;

    public UserDetails loadUserByApiKey(String apiKey) {
        return authUserRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with API key: " + apiKey));
    }

    public String generateApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}