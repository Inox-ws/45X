package com.inox.x45.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceListItemResponse(
    Long id,
    String invoiceNumber,
    String customerName,
    LocalDate invoiceDate,
    BigDecimal amount,
    String currency,
    String status
) {}
