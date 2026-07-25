package com.driftstay.auth.service;

import com.driftstay.config.JwtProperties;
import com.driftstay.security.JwtKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Service for JWT token operations: generation, validation, and parsing.
 * Handles both access tokens (short-lived) and refresh tokens (long-lived).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtKeyProvider jwtKeyProvider;
    private final JwtProperties jwtProperties;

    /**
     * Generate a JWT access token for the given user.
     *
     * @param userId   The user's ID
     * @param email    The user's email
     * @param roles    The user's roles
     * @return Signed JWT access token string
     */
    public String generateAccessToken(Long userId, String email, List<String> roles) {
        long expirationMs = Long.parseLong(jwtProperties.getAccessTokenExpiration());
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String token = Jwts.builder()
                .subject(email)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", "access")
                .signWith(jwtKeyProvider.getSigningKey())
                .compact();

        log.debug("Generated access token for user {} (email: {}), expires in {}ms",
                userId, email, expirationMs);

        return token;
    }

    /**
     * Generate a refresh token value (random string).
     * The refresh token is stored as a SHA-256 hash in the database.
     *
     * @return Random refresh token string
     */
    public String generateRefreshTokenValue() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Get the expiration time for refresh tokens from configuration.
     */
    public long getRefreshTokenExpirationMs() {
        return Long.parseLong(jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * Get the expiration time for access tokens from configuration.
     */
    public long getAccessTokenExpirationMs() {
        return Long.parseLong(jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Validate a JWT access token.
     *
     * @param token The JWT token to validate
     * @return true if the token is valid
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported token: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract the user ID from a JWT token.
     *
     * @param token The JWT token
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extract the email from a JWT token.
     *
     * @param token The JWT token
     * @return Email claim
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * Extract roles from a JWT token.
     *
     * @param token The JWT token
     * @return List of role strings
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roles", List.class);
    }

    /**
     * Parse and validate a JWT token, returning the claims.
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(jwtKeyProvider.getSigningKey())
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
