package se.reviewservice.mongoDB.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.reviewservice.mongoDB.service.ApiKeyService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = extractApiKey(request);

        if (apiKey != null) {
            try {
                UserDetails userDetails = apiKeyService.loadUserByApiKey(apiKey);

                // Skapa nya roller för att täcka alla möjliga formatvarianter
                List<GrantedAuthority> authorities = new ArrayList<>();

                // Lägg till ursprungliga roller
                authorities.addAll(userDetails.getAuthorities());

                // Lägg till roller utan ROLE_ prefix
                for (GrantedAuthority authority : new ArrayList<>(userDetails.getAuthorities())) {
                    String auth = authority.getAuthority();
                    if (auth.startsWith("ROLE_")) {
                        authorities.add(new SimpleGrantedAuthority(auth.substring(5)));
                    }
                }

                // Lägg till gruppspecifika roller baserade på användarnamn
                String username = userDetails.getUsername().toLowerCase();
                if (username.startsWith("group")) {
                    // Exempelvis om username är "group5"
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + username.toUpperCase()));
                    authorities.add(new SimpleGrantedAuthority(username.toUpperCase()));
                    authorities.add(new SimpleGrantedAuthority("GROUP" + username.substring(5).toUpperCase()));
                    authorities.add(new SimpleGrantedAuthority("GROUP_" + username.substring(5).toUpperCase()));
                }

                // Skapa autentiseringsobjekt med roller
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails.getUsername(),
                                null,
                                authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (UsernameNotFoundException e) {
                // Fortsätt kedjan - ingen giltigt API-nyckel hittades
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractApiKey(HttpServletRequest request) {
        // Första försök: X-API-KEY header (exakt match)
        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey != null) {
            return apiKey;
        }

        // Andra försök: x-api-key header (mindre känslig för små/stora bokstäver)
        apiKey = request.getHeader("x-api-key");
        if (apiKey != null) {
            return apiKey;
        }

        // Tredje försök: Authorization header (för Bearer token format)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            apiKey = authHeader.substring(7);
            return apiKey;
        }

        // Fjärde försök: sök igenom alla headers (fallback)
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase("x-api-key")) {
                apiKey = request.getHeader(headerName);
                return apiKey;
            }
        }

        return null;
    }
}