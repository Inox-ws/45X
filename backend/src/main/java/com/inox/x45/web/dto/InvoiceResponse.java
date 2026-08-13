package com.inox.x45.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceResponse(
    Long id,
    String invoiceNumber,
    Long customerId,
    String customerName,
    LocalDate invoiceDate,
    BigDecimal amount,
    String currency,
    String status,
    Long documentId
) {}
