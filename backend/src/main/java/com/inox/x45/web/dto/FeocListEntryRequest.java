package com.inox.x45.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record FeocListEntryRequest(
    @NotBlank String entryType,
    @NotBlank String name,
    @NotBlank String status,
    String notes,
    LocalDate effectiveFrom
) {}
