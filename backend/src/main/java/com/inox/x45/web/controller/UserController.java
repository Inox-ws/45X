package com.inox.x45.web.controller;

import com.inox.x45.service.UserManagementService;
import com.inox.x45.web.dto.CreateUserRequest;
import com.inox.x45.web.dto.UpdateActiveRequest;
import com.inox.x45.web.dto.UpdateRolesRequest;
import com.inox.x45.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** User Management (Section 5). Admin-only. */
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public List<UserResponse> list() {
        return userManagementService.listUsers();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userManagementService.getUser(id);
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        return userManagementService.createUser(request, authentication);
    }

    @PutMapping("/{id}/roles")
    public UserResponse updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateRolesRequest request, Authentication authentication) {
        return userManagementService.updateRoles(id, request.roles(), authentication);
    }

    @PatchMapping("/{id}/active")
    public UserResponse setActive(@PathVariable Long id, @RequestBody UpdateActiveRequest request, Authentication authentication) {
        return userManagementService.setActive(id, request.active(), authentication);
    }
}
