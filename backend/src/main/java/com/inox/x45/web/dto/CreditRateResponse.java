package com.inox.x45.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditRateResponse(
    Long id,
    String componentType,
    BigDecimal ratePerWatt,
    LocalDate effectiveFrom,
    LocalDate effectiveTo
) {}
