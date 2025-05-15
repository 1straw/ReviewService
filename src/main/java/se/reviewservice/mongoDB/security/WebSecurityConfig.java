package se.reviewservice.mongoDB.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private ApiKeyFilter apiKeyFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Tillåt Swagger UI och dess resurser utan autentisering
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Tillåt inloggning utan autentisering
                        .requestMatchers("/api/auth/login").permitAll()
                        // OBS! Vi tar bort /api/auth/register från permitAll för att blockera publik registrering
                        // .requestMatchers("/api/auth/register").permitAll()
                        // Lägg till specialendpoints för GET-anrop
                        .requestMatchers("GET", "/api/v1/products/*/reviews").hasAnyRole("USER", "ADMIN", "FRONTEND_GROUP")
                        // Lägg till specialregler för vilka roller som kan ändra data
                        .requestMatchers("POST", "/api/v1/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("PUT", "/api/v1/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("DELETE", "/api/v1/**").hasAnyRole("USER", "ADMIN")
                        // Alla andra anrop kräver autentisering
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Lägg till API-nyckel filter FÖRE JWT-filter
        http.addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}