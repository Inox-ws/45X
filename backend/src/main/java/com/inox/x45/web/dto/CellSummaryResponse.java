package com.inox.x45.web.dto;

import java.math.BigDecimal;

public record CellSummaryResponse(
    Long id,
    String cellSerialNumber,
    String batch,
    String lot,
    BigDecimal wattage,
    String countryOfOrigin,
    String feocStatus,
    SupplierSummaryResponse supplier
) {}
