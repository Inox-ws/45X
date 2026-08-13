package com.inox.x45.web.controller;

import com.inox.x45.domain.AppUser;
import com.inox.x45.domain.UserRoleAssignment;
import com.inox.x45.repository.AppUserRepository;
import com.inox.x45.repository.UserRoleAssignmentRepository;
import com.inox.x45.security.LocalJwtProperties;
import com.inox.x45.security.LocalJwtService;
import com.inox.x45.web.dto.LoginRequest;
import com.inox.x45.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 'local' profile only fallback login (Section 3). Issues the JWTs
 * LocalJwtAuthenticationFilter validates. Never present when the 'azure'
 * profile (real Entra ID) is active.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Profile("local")
public class LocalAuthController {

    private final AppUserRepository userRepository;
    private final UserRoleAssignmentRepository roleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocalJwtService jwtService;
    private final LocalJwtProperties jwtProperties;

    public LocalAuthController(AppUserRepository userRepository,
                                UserRoleAssignmentRepository roleAssignmentRepository,
                                PasswordEncoder passwordEncoder,
                                LocalJwtService jwtService,
                                LocalJwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
            .filter(AppUser::isActive)
            .filter(u -> u.getPasswordHash() != null && passwordEncoder.matches(request.password(), u.getPasswordHash()))
            .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<String> roles = roleAssignmentRepository.findByUserId(user.getId()).stream()
            .map(UserRoleAssignment::getRole)
            .map(role -> role.getName())
            .toList();

        String token = jwtService.issueToken(user.getEmail(), user.getFullName(), roles);
        return ResponseEntity.ok(new LoginResponse(
            token, jwtProperties.getExpirationMinutes(), user.getFullName(), user.getEmail(), roles));
    }
}
