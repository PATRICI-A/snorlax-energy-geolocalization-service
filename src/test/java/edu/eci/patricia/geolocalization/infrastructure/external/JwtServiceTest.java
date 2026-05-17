package edu.eci.patricia.geolocalization.infrastructure.external;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-only-32c";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    private String buildToken(String userId, String email, long expiryMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key)
                .compact();
    }

    @Test
    void extractUserId_validToken_returnsSubject() {
        String token = buildToken("user-abc", "user@escuelaing.edu.co", 60_000);
        assertThat(jwtService.extractUserId(token)).isEqualTo("user-abc");
    }

    @Test
    void extractEmail_validToken_returnsEmail() {
        String token = buildToken("user-abc", "user@escuelaing.edu.co", 60_000);
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@escuelaing.edu.co");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = buildToken("user-abc", "user@escuelaing.edu.co", 60_000);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        String token = buildToken("user-abc", "user@escuelaing.edu.co", -1000);
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("not.a.valid.token")).isFalse();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = buildToken("user-abc", "user@escuelaing.edu.co", 60_000);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }
}
