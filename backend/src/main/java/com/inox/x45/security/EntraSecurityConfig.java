package com.inox.x45.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;

import java.util.List;

/**
 * 'azure' profile only: real Entra ID JWT validation. Spring Boot's
 * issuer-uri auto-configuration validates issuer, signature, and expiry, but
 * NOT audience - this bean adds that check explicitly (Section 12 requires
 * validating issuer, audience, signature, and expiry).
 */
@Configuration
@Profile("azure")
public class EntraSecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder(EntraProperties entraProperties) {
        String issuerUri = "https://login.microsoftonline.com/" + entraProperties.getTenantId() + "/v2.0";
        var decoder = (org.springframework.security.oauth2.jwt.NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = jwt -> {
            List<String> audiences = jwt.getAudience();
            if (audiences != null && audiences.contains(entraProperties.getApiAudience())) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                "invalid_token", "Required audience '" + entraProperties.getApiAudience() + "' not present", null));
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    public EntraJwtRoleConverter entraJwtRoleConverter() {
        return new EntraJwtRoleConverter();
    }
}
