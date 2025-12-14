package com.assignment.shop.security.helper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtHelper {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long tokenValidity;

    public String getUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public <T> T getClaim(String token, Function<Claims, T> resolver) {
        Claims claims = getAllClaims(token);
        return resolver.apply(claims);
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            String username = getUsername(token);
            return username.equals(user.getUsername()) && !isExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String generate(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, user.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String username) {
        long currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + tokenValidity))
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }

    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
