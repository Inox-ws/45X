package com.inox.x45.web.dto;

import java.util.List;

public record CurrentUserResponse(
    String email,
    String displayName,
    List<String> roles
) {}
