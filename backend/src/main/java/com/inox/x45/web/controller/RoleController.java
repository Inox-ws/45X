package com.inox.x45.web.controller;

import com.inox.x45.repository.AppRoleRepository;
import com.inox.x45.web.dto.RoleResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The fixed set of roles (Section 4) - lookup for the User Management role-picker. */
@RestController
@RequestMapping("/api/v1/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final AppRoleRepository appRoleRepository;

    public RoleController(AppRoleRepository appRoleRepository) {
        this.appRoleRepository = appRoleRepository;
    }

    @GetMapping
    public List<RoleResponse> list() {
        return appRoleRepository.findAll().stream()
            .map(role -> new RoleResponse(role.getId(), role.getName(), role.getDescription()))
            .toList();
    }
}
