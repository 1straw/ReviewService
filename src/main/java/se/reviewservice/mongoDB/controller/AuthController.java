package se.reviewservice.mongoDB.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.reviewservice.mongoDB.model.AuthUser;
import se.reviewservice.mongoDB.model.AuthRequest;
import se.reviewservice.mongoDB.model.AuthResponse;
import se.reviewservice.mongoDB.model.RegisterRequest;
import se.reviewservice.mongoDB.security.JwtUtil;
import se.reviewservice.mongoDB.service.AuthUserService;
import se.reviewservice.mongoDB.service.ApiKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API för autentisering och registrering")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthUserService userService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Operation(summary = "Login", description = "Logga in och få en JWT-token")
    @PostMapping("/login")
    public ResponseEntity<?> createAuthToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed for user: {}", authRequest.getUsername());
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        logger.debug("Generated JWT token for user: {}", authRequest.getUsername());

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @Operation(summary = "Register", description = "Registrera en ny användare")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            AuthUser user = new AuthUser();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setEmail(registerRequest.getEmail());
            user.setRole(registerRequest.getRole());

            // Om användaren väljer API-nyckel, generera en
            if (registerRequest.isUseApiKey()) {
                user.setApiKey(apiKeyService.generateApiKey());
            }

            AuthUser savedUser = userService.registerUser(user);
            logger.info("User registered successfully: {}", registerRequest.getUsername());

            // Returnera API-nyckeln om den genererades
            if (savedUser.getApiKey() != null) {
                return ResponseEntity.ok("User registered successfully. API Key: " + savedUser.getApiKey());
            } else {
                return ResponseEntity.ok("User registered successfully");
            }
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}