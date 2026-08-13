package com.inox.x45.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Issues and validates the HMAC-signed JWTs used by the 'local' profile's
 * fallback auth (Section 3). Keys.hmacShaKeyFor picks the strongest HMAC
 * algorithm the configured secret's length supports (HS256/384/512) - it
 * doesn't need to be pinned to exactly HS256. The 'azure' profile validates
 * real Entra ID tokens instead (see EntraSecurityConfig) and never touches
 * this class.
 */
@Service
@Profile("local")
public class LocalJwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String NAME_CLAIM = "name";

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public LocalJwtService(LocalJwtProperties properties) {
        if (properties.getSecret() == null || properties.getSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                "x45.local-auth.jwt.secret must be set and at least 32 bytes for HS256 (local profile only)");
        }
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = properties.getExpirationMinutes();
    }

    public String issueToken(String email, String displayName, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(email)
            .claim(NAME_CLAIM, displayName)
            .claim(ROLES_CLAIM, roles)
            .issuedAt(java.util.Date.from(now))
            .expiration(java.util.Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
            .signWith(signingKey)
            .compact();
    }

    public String nameOf(Claims claims) {
        return claims.get(NAME_CLAIM, String.class);
    }

    /** @throws JwtException if the token is malformed, expired, or has an invalid signature. */
    public Claims parseAndValidate(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> rolesOf(Claims claims) {
        return (List<String>) claims.get(ROLES_CLAIM, List.class);
    }
}
