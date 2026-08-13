package com.inox.x45.ocr;

import java.math.BigDecimal;

public record ExtractedLineItem(
    String description,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal amount,
    /** Nullable - only present when the line item states a wattage (Section 6.1). */
    BigDecimal wattage
) {}
