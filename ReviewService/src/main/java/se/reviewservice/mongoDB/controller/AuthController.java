package se.reviewservice.mongoDB.controller;

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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthUserService userService;

    @Autowired
    private ApiKeyService apiKeyService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userService.loadUserByUsername(authRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
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

        // Returnera API-nyckeln om den genererades
        if (savedUser.getApiKey() != null) {
            return ResponseEntity.ok("User registered successfully. API Key: " + savedUser.getApiKey());
        } else {
            return ResponseEntity.ok("User registered successfully");
        }
    }
}