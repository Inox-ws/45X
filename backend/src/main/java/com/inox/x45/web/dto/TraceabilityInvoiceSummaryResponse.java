package com.inox.x45.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TraceabilityInvoiceSummaryResponse(
    Long id,
    String invoiceNumber,
    LocalDate invoiceDate,
    BigDecimal amount,
    String currency,
    String status,
    String customerName
) {}
