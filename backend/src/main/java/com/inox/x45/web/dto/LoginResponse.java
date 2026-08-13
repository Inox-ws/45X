package com.inox.x45.web.dto;

import java.util.List;

public record LoginResponse(
    String accessToken,
    long expiresInMinutes,
    String displayName,
    String email,
    List<String> roles
) {}
