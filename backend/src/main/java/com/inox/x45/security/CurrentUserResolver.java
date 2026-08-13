package com.inox.x45.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves (email, displayName, roles) uniformly whether the request was
 * authenticated by the 'local' JWT fallback (principal = AuthenticatedUser)
 * or by the 'azure' profile's real Entra ID token (principal = Jwt).
 */
@Component
public class CurrentUserResolver {

    public record Resolved(String email, String displayName, List<String> roles) {}

    public Resolved resolve(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring("ROLE_".length()))
            .toList();

        if (authentication.getPrincipal() instanceof AuthenticatedUser localUser) {
            return new Resolved(localUser.email(), localUser.displayName(), roles);
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt jwt = jwtAuthenticationToken.getToken();
            String email = firstNonBlank(jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("upn"), jwt.getClaimAsString("email"), jwt.getSubject());
            String displayName = firstNonBlank(jwt.getClaimAsString("name"), email);
            return new Resolved(email, displayName, roles);
        }

        return new Resolved(authentication.getName(), authentication.getName(), roles);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
