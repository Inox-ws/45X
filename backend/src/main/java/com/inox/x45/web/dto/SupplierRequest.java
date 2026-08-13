package com.inox.x45.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(
    @NotBlank String name,
    @NotBlank String countryOfOrigin,
    String feocStatus,
    String feocNotes,
    String materialInfo,
    boolean active
) {}
