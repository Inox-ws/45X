package com.inox.x45.security;

/** Principal set by LocalJwtAuthenticationFilter for the 'local' profile's fallback auth. */
public record AuthenticatedUser(String email, String displayName) {}
