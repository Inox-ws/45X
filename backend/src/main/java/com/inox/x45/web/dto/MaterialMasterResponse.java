package com.inox.x45.web.dto;

import java.time.Instant;

public record MaterialMasterResponse(
    Long id,
    String materialCode,
    String description,
    String uom,
    String source,
    Instant lastSyncedAt
) {}
