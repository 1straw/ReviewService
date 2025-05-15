package se.reviewservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import se.reviewservice.mongoDB.model.AuthUser;
import se.reviewservice.mongoDB.repository.AuthUserRepository;
import se.reviewservice.mongoDB.service.ApiKeyService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    public void run(String... args) throws Exception {
        createFrontendGroupUsers();
    }

    private void createFrontendGroupUsers() {
        // Definiera grupper och deras e-postadresser
        Map<String, String> frontendGroups = new HashMap<>();
        frontendGroups.put("group4", "group4@example.com");
        frontendGroups.put("group5", "group5@example.com");
        frontendGroups.put("group6", "group6@example.com");

        // Skapa användare för varje grupp om de inte redan finns
        for (Map.Entry<String, String> entry : frontendGroups.entrySet()) {
            String groupName = entry.getKey();
            String email = entry.getValue();

            // Kolla om användaren redan finns
            Optional<AuthUser> existingUser = authUserRepository.findByUsername(groupName);

            if (!existingUser.isPresent()) {
                AuthUser user = new AuthUser();
                user.setUsername(groupName);
                user.setPassword("NOT_USED_WITH_API_KEY"); // Sätt ett ogiltigt lösenord
                user.setEmail(email);
                user.setRole("FRONTEND_GROUP"); // Särskild roll för frontend-grupper

                // Generera API-nyckel
                String apiKey = apiKeyService.generateApiKey();
                user.setApiKey(apiKey);

                // Spara användaren
                authUserRepository.save(user);

                // Skriv ut API-nyckeln i konsolen för att kunna dela med grupperna
                System.out.println("=================================================");
                System.out.println("Created user for: " + groupName);
                System.out.println("API KEY: " + apiKey);
                System.out.println("=================================================");
            } else {
                // Om användaren redan finns, visa API-nyckeln om den finns
                AuthUser user = existingUser.get();
                if (user.getApiKey() != null && !user.getApiKey().isEmpty()) {
                    System.out.println("User for " + groupName + " already exists with API key: " + user.getApiKey());
                } else {
                    // Om användaren finns men inte har en API-nyckel, skapa en
                    String apiKey = apiKeyService.generateApiKey();
                    user.setApiKey(apiKey);
                    authUserRepository.save(user);

                    System.out.println("=================================================");
                    System.out.println("Added API key to existing user: " + groupName);
                    System.out.println("API KEY: " + apiKey);
                    System.out.println("=================================================");
                }
            }
        }
    }
}