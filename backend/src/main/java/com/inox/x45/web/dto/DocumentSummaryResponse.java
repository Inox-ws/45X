package com.inox.x45.web.dto;

import java.time.Instant;

public record DocumentSummaryResponse(Long id, String documentType, String fileName, Instant uploadedAt) {}
