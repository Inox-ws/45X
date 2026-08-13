package com.inox.x45.web.dto;

import java.time.LocalDate;

public record FeocListEntryResponse(
    Long id,
    String entryType,
    String name,
    String status,
    String notes,
    LocalDate effectiveFrom
) {}
