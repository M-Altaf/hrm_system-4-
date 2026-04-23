package com.example.hrm.system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET =
            "your-very-secret-key-must-be-at-least-32-chars!!";
    private static final long EXPIRATION_MS = 86400000; // 24 hours

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // ── Generate token ────────────────────────────────────────────────
    public String generateToken(Long userId, String username,
                                String email,  String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("email",  email)
                .claim("role",  "ROLE_" + role)     // ✅ prefix added here
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Extractors ────────────────────────────────────────────────────
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ── Validate ──────────────────────────────────────────────────────
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // ── Parse ─────────────────────────────────────────────────────────
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}