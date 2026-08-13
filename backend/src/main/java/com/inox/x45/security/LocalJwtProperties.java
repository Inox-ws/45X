package com.inox.x45.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Config for the 'local' profile's JWT fallback auth (Section 3). Never used
 * when the 'azure' profile (real Entra ID) is active.
 */
@Component
@ConfigurationProperties(prefix = "x45.local-auth.jwt")
public class LocalJwtProperties {

    /** HMAC signing secret. Must be >= 32 bytes. Dev-only - never reused for the 'azure' profile. */
    private String secret;

    private long expirationMinutes = 480;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }
}
