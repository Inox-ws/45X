package com.inox.x45.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConfirmInvoiceRequest(
    @NotBlank String invoiceNumber,
    @NotNull LocalDate invoiceDate,
    @NotNull Long customerId,
    @NotNull @Positive BigDecimal amount,
    @NotBlank String currency
) {}
