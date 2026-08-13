package com.inox.x45.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Consistent list envelope for every paginated endpoint (Section 13). */
public record PageResponse<T>(List<T> items, int page, int size, long total) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
