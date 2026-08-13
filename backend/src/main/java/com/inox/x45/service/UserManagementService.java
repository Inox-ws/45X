package com.inox.x45.service;

import com.inox.x45.audit.AuditService;
import com.inox.x45.domain.AppRole;
import com.inox.x45.domain.AppUser;
import com.inox.x45.domain.UserRoleAssignment;
import com.inox.x45.repository.AppRoleRepository;
import com.inox.x45.repository.AppUserRepository;
import com.inox.x45.repository.UserRoleAssignmentRepository;
import com.inox.x45.security.CurrentUserResolver;
import com.inox.x45.web.dto.CreateUserRequest;
import com.inox.x45.web.dto.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** User Management (Section 5): CRUD users, assign roles. Admin-only (enforced at the controller). */
@Service
public class UserManagementService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final CurrentUserResolver currentUserResolver;

    public UserManagementService(AppUserRepository appUserRepository, AppRoleRepository appRoleRepository,
                                  UserRoleAssignmentRepository userRoleAssignmentRepository, PasswordEncoder passwordEncoder,
                                  AuditService auditService, CurrentUserResolver currentUserResolver) {
        this.appUserRepository = appUserRepository;
        this.appRoleRepository = appRoleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.currentUserResolver = currentUserResolver;
    }

    public List<UserResponse> listUsers() {
        return appUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUser(Long id) {
        return toResponse(getUserOrThrow(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, Authentication actor) {
        if (appUserRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists: " + request.email());
        }
        AppUser user = new AppUser();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setActive(true);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user = appUserRepository.save(user);

        assignRoles(user, request.roles(), actor);

        UserResponse response = toResponse(user);
        auditService.record(actor, "CREATE", "AppUser", user.getId(), null, response);
        return response;
    }

    @Transactional
    public UserResponse updateRoles(Long id, List<String> roleNames, Authentication actor) {
        AppUser user = getUserOrThrow(id);
        UserResponse before = toResponse(user);

        userRoleAssignmentRepository.deleteByUserId(user.getId());
        assignRoles(user, roleNames, actor);

        UserResponse after = toResponse(user);
        auditService.record(actor, "UPDATE_ROLES", "AppUser", user.getId(), before, after);
        return after;
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active, Authentication actor) {
        AppUser user = getUserOrThrow(id);
        UserResponse before = toResponse(user);
        user.setActive(active);
        user = appUserRepository.save(user);
        UserResponse after = toResponse(user);
        auditService.record(actor, active ? "ACTIVATE" : "DEACTIVATE", "AppUser", user.getId(), before, after);
        return after;
    }

    private void assignRoles(AppUser user, List<String> roleNames, Authentication actor) {
        String actorEmail = currentUserResolver.resolve(actor).email();
        AppUser assignedBy = actorEmail == null ? null : appUserRepository.findByEmailIgnoreCase(actorEmail).orElse(null);

        for (String roleName : roleNames) {
            AppRole role = appRoleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + roleName));
            UserRoleAssignment assignment = new UserRoleAssignment();
            assignment.setUser(user);
            assignment.setRole(role);
            assignment.setAssignedAt(Instant.now());
            assignment.setAssignedBy(assignedBy);
            userRoleAssignmentRepository.save(assignment);
        }
    }

    private AppUser getUserOrThrow(Long id) {
        return appUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    private UserResponse toResponse(AppUser user) {
        List<String> roles = userRoleAssignmentRepository.findByUserId(user.getId()).stream()
            .map(a -> a.getRole().getName())
            .toList();
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.isActive(), roles);
    }
}
