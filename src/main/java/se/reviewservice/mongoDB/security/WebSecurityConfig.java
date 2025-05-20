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

                        // VIKTIGT: Lägg till tillåtelse för debug-endpointerna
                        .requestMatchers("/api/debug/**").permitAll()

                        // För testning: tillåt alla GET-förfrågningar på product reviews utan autentisering
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/group-reviews").permitAll()

                        // API-vägar som är tillgängliga via API-nyckel (bara GET)
                        // Kommenterad för att tillåta testing utan autentisering
                        // .requestMatchers(HttpMethod.GET, "/api/v1/products/reviews").hasAnyAuthority(
                        //         "ROLE_GROUP4", "ROLE_GROUP5", "ROLE_GROUP6", "ROLE_ADMIN", "ROLE_FRONTEND_GROUP")
                        // .requestMatchers(HttpMethod.GET, "/api/v1/products/all-reviews").hasAnyAuthority(
                        //         "ROLE_GROUP4", "ROLE_GROUP5", "ROLE_GROUP6", "ROLE_ADMIN", "ROLE_FRONTEND_GROUP")

                        // Blockera alla andra HTTP-metoder från API-nyckelanvändare
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasAuthority("ROLE_ADMIN")

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