package com.inox.x45.web.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateRolesRequest(@NotEmpty List<String> roles) {}
