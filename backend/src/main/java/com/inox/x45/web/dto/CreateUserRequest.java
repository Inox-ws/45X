package com.inox.x45.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateUserRequest(
    @NotBlank String fullName,
    @NotBlank @Email String email,
    /** Local-dev fallback only (Section 3) - ignored once the 'azure' profile provisions users via Entra ID. */
    String password,
    @NotEmpty List<String> roles
) {}
