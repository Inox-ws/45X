package com.inox.x45.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
    @NotBlank String name,
    String address,
    String contactName,
    @Email String contactEmail,
    String contactPhone,
    boolean active
) {}
