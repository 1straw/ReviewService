package se.reviewservice.mongoDB.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.reviewservice.mongoDB.service.ApiKeyService;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String apiKey = request.getHeader(API_KEY_HEADER);

        // Om API-nyckel finns och ingen autentisering är satt än
        if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = apiKeyService.loadUserByApiKey(apiKey);

                // Om användaren hittades, skapa en autentiseringstoken
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                // Logga men fortsätt, kanske använder de JWT istället
                logger.warn("Authentication via API key failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

}