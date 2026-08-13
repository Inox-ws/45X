package com.inox.x45.ocr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Pre-filled invoice fields from OCR (Section 5, Section 9) - the user
 * confirms/edits these before the Invoice record is actually created, so
 * every field here is a best-effort suggestion, not a validated value.
 */
public record ExtractedInvoiceData(
    String invoiceNumber,
    LocalDate invoiceDate,
    String customerName,
    BigDecimal amount,
    String currency,
    List<ExtractedLineItem> lineItems,
    /** Nullable - total wattage across line items, when the document states it. */
    BigDecimal totalWattage
) {}
