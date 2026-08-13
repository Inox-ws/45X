package com.inox.x45.web.dto;

public record CustomerResponse(
    Long id,
    String name,
    String address,
    String contactName,
    String contactEmail,
    String contactPhone,
    boolean active
) {}
