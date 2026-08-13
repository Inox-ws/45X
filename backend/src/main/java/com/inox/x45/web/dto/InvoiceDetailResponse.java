package com.inox.x45.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceDetailResponse(
    Long id,
    String invoiceNumber,
    Long customerId,
    String customerName,
    LocalDate invoiceDate,
    BigDecimal amount,
    String currency,
    String status,
    String source,
    List<DocumentSummaryResponse> documents
) {}
