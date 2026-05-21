package edu.eci.patricia.geolocalization.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class KongAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                filterChain.doFilter(request, response);
                return;
            }

            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String json = new String(decoded, StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode claims = mapper.readTree(json);

            String userId = claims.has("sub") ? claims.get("sub").asText() : null;
            String role = claims.has("role") ? claims.get("role").asText() : "USER";

            if (userId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.warn("JWT inválido: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
