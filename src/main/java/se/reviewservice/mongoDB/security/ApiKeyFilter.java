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
import java.util.Enumeration;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("\n--- ApiKeyFilter start ---");
        System.out.println("Request URI: " + request.getRequestURI());

        // Sök efter API-nyckeln i headerna, oavsett skiftläge
        String apiKey = null;

        // Först, försök med exakt headernamn
        apiKey = request.getHeader(API_KEY_HEADER);
        System.out.println("API-nyckel från exakt header " + API_KEY_HEADER + ": " + apiKey);

        // Om det inte finns, försök med skiftlägesokänslig matchning
        if (apiKey == null) {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                System.out.println("Header: " + headerName + " = " + request.getHeader(headerName));
                if (API_KEY_HEADER.equalsIgnoreCase(headerName)) {
                    apiKey = request.getHeader(headerName);
                    System.out.println("Hittade API-nyckel i header " + headerName + ": " + apiKey);
                    break;
                }
            }
        }

        // Om API-nyckel fanns och ingen autentisering är satt än
        if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = apiKeyService.loadUserByApiKey(apiKey);
                System.out.println("Hittade användare för API-nyckel: " + userDetails.getUsername());

                // Om användaren hittades, skapa en autentiseringstoken
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Satte autentisering för användare: " + userDetails.getUsername());
            } catch (Exception e) {
                // Logga men fortsätt, kanske använder de JWT istället
                logger.warn("Authentication via API key failed: " + e.getMessage());
                System.out.println("Autentisering via API-nyckel misslyckades: " + e.getMessage());
            }
        } else {
            System.out.println("Ingen API-nyckel hittades eller autentisering redan satt");
        }

        System.out.println("--- ApiKeyFilter end ---\n");
        filterChain.doFilter(request, response);
    }
}