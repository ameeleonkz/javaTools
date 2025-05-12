package com.otpservice.service;

import com.otpservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtService {
    private final Key key;
    private static final long TOKEN_VALIDITY_MINUTES = 60;
    
    public JwtService() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }
    
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getUsername())
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plus(TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES)))
            .signWith(key)
            .compact();
    }
    
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }
    
    public String getRoleFromToken(String token) {
        return validateToken(token).get("role", String.class);
    }
    
    public Long getUserIdFromToken(String token) {
        return validateToken(token).get("userId", Long.class);
    }
} 