package com.inox.x45.web.dto;

import java.util.List;

public record UserResponse(Long id, String fullName, String email, boolean active, List<String> roles) {}
