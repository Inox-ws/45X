package com.inox.x45.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ModuleSummaryResponse(
    Long id,
    String moduleSerialNumber,
    BigDecimal wattage,
    LocalDate productionDate,
    List<CellSummaryResponse> cells
) {}
