package com.inox.x45.web.dto;

/** sasUrl is null when the active storage backend has no direct-download mechanism (local dev) - fall back to /raw. */
public record DownloadUrlResponse(String sasUrl) {}
