package com.inox.x45.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditRateRequest(
    @NotBlank String componentType,
    @NotNull @Positive BigDecimal ratePerWatt,
    @NotNull LocalDate effectiveFrom,
    LocalDate effectiveTo
) {}
