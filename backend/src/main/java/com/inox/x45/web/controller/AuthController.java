package com.inox.x45.web.controller;

import com.inox.x45.security.CurrentUserResolver;
import com.inox.x45.web.dto.CurrentUserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Works against either the 'local' JWT fallback or a real Entra ID token (Section 3). */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CurrentUserResolver currentUserResolver;

    public AuthController(CurrentUserResolver currentUserResolver) {
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        CurrentUserResolver.Resolved resolved = currentUserResolver.resolve(authentication);
        return new CurrentUserResponse(resolved.email(), resolved.displayName(), resolved.roles());
    }
}
