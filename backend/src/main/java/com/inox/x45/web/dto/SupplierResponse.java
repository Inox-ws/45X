package com.inox.x45.web.dto;

public record SupplierResponse(
    Long id,
    String name,
    String countryOfOrigin,
    String feocStatus,
    String feocNotes,
    String materialInfo,
    boolean active
) {}
