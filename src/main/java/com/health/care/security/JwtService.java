package com.health.care.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import com.health.care.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getExpirationMs());

        logger.debug("Generating JWT token for user: {}", username);
        String token = Jwts.builder()
                .subject(username)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
        logger.debug("JWT token generated successfully for user: {}", username);
        return token;
    }

    public String extractUsername(String token) {
        try {
            Claims claims = parseClaims(token);
            String username = claims.getSubject();
            logger.debug("Extracted username from token: {}", username);
            return username;
        } catch (Exception e) {
            logger.warn("Failed to extract username from token", e);
            throw e;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            logger.debug("Token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Token has expired");
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationMs() {
        return jwtProperties.getExpirationMs();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
