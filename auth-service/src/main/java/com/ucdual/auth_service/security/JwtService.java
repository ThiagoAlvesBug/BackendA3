package com.ucdual.auth_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final String SECRET = "26322472-aabc-4328-95d2-b61dbe94d234";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<SimpleGrantedAuthority> extractRoles(String token) {
        List<String> roles = extractAllClaims(token).get("roles", List.class);

        if (roles == null) {
            return List.of();
        }

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
