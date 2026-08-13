package com.inox.x45.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;

/**
 * Maps an Entra ID token's "roles" claim (App Roles assigned to the signed-in
 * user in the API app registration) onto the same ROLE_X authorities the
 * 'local' profile's fallback auth produces, so @PreAuthorize checks work
 * identically regardless of which profile is active (Section 4, Section 12).
 *
 * This assumes the API app registration's App Roles are named exactly
 * FINANCE / LOGISTICS / PRODUCTION / MANAGEMENT / ADMIN - create them to
 * match Section 4 when setting up the app registration.
 */
public class EntraJwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        Collection<GrantedAuthority> authorities = (roles == null ? List.<String>of() : roles).stream()
            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
            .toList();
        return new JwtAuthenticationToken(jwt, authorities);
    }
}
