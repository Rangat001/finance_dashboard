package com.example.finance.finance.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWTutil
 * Purpose:
 * - Generates JWT tokens after successful login
 * - Validates tokens (signature + expiration)
 * - Extracts claims like subject (email) and role
 *
 * Design:
 * - subject (sub) = user email (unique)
 * - role claim = "ADMIN" | "ANALYST" | "VIEWER"
 */
@Component
public class JWTutil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    // Token TTL (example: 30 days).
    private static final long TOKEN_TTL_MILLIS = 1000L * 60 * 60 * 24 * 30;

    /**
     * getSigningKey()
     * Purpose: Builds the signing key used to sign and verify JWTs.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * generateToken(email, role)
     * Purpose: Creates a JWT for the logged-in user.
     * Stores:
     * - subject: email
     * - claim "role": role string (ADMIN/ANALYST/VIEWER)
     */
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Eliminate DB call at controller to check the Role

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_TTL_MILLIS))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * extractUsername(token)
     * Purpose: Returns the subject (email) from JWT.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * extractRole(token)
     * Purpose: Returns the user's role claim from JWT.
     */
    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? null : role.toString();
    }

    /**
     * validateToken(token)
     * Purpose: Returns true if token is valid (signature ok) and not expired.
     * Any parsing exceptions => invalid token.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * extractAllClaims(token)
     * Purpose: Parses JWT and returns claims. This verifies signature too.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * isTokenExpiringSoon(token)
     * Purpose: Helper for "renew soon" logic (future feature).
     */
    public boolean isTokenExpiringSoon(String token) {
        Claims claims = extractAllClaims(token);
        Date exp = claims.getExpiration();
        long timeLeft = exp.getTime() - System.currentTimeMillis();
        return timeLeft < 1000L * 60 * 60 * 24 * 3; // < 3 days
    }
}