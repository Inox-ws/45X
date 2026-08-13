package com.inox.x45.web.dto;

import jakarta.validation.constraints.NotBlank;

public record MaterialMasterRequest(
    @NotBlank String materialCode,
    String description,
    String uom
) {}
