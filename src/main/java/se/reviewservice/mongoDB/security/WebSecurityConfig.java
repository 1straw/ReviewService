package se.reviewservice.mongoDB.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        // Tillåt ALLA Swagger-relaterade resurser utan autentisering
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**").permitAll()
                        // Tillåt inloggning utan autentisering
                        .requestMatchers("/api/auth/login").permitAll()

                        // API-vägar som kräver autentisering och specifika roller
                        .requestMatchers(HttpMethod.GET, "/api/v1/group-reviews")
                        .hasAnyAuthority("ROLE_GROUP4", "GROUP4", "ROLE_GROUP5", "GROUP5", "ROLE_GROUP6", "GROUP6",
                                "ROLE_ADMIN", "ADMIN", "group4", "group5", "group6")
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/reviews")
                        .hasAnyAuthority("ROLE_GROUP4", "GROUP4", "ROLE_GROUP5", "GROUP5", "ROLE_GROUP6", "GROUP6",
                                "ROLE_ADMIN", "ADMIN", "group4", "group5", "group6")
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/all-reviews")
                        .hasAnyAuthority("ROLE_GROUP4", "GROUP4", "ROLE_GROUP5", "GROUP5", "ROLE_GROUP6", "GROUP6",
                                "ROLE_ADMIN", "ADMIN", "group4", "group5", "group6")

                        // Blockera alla andra HTTP-metoder från API-nyckelanvändare
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")

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