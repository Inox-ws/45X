package com.inox.x45.web.controller;

import com.inox.x45.audit.AuditService;
import com.inox.x45.domain.FeocListEntry;
import com.inox.x45.domain.enums.FeocListEntryType;
import com.inox.x45.domain.enums.FeocListStatus;
import com.inox.x45.repository.FeocListEntryRepository;
import com.inox.x45.web.dto.FeocListEntryRequest;
import com.inox.x45.web.dto.FeocListEntryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Master Data - FEOC/PFE country & entity list (Section 5, Section 6.2). Admin-only. */
@RestController
@RequestMapping("/api/v1/master-data/feoc-list")
@PreAuthorize("hasRole('ADMIN')")
public class FeocListEntryController {

    private final FeocListEntryRepository feocListEntryRepository;
    private final AuditService auditService;

    public FeocListEntryController(FeocListEntryRepository feocListEntryRepository, AuditService auditService) {
        this.feocListEntryRepository = feocListEntryRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<FeocListEntryResponse> list() {
        return feocListEntryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public FeocListEntryResponse get(@PathVariable Long id) {
        return toResponse(getOrThrow(id));
    }

    @PostMapping
    public FeocListEntryResponse create(@Valid @RequestBody FeocListEntryRequest request, Authentication authentication) {
        FeocListEntry entry = new FeocListEntry();
        applyRequest(entry, request);
        entry = feocListEntryRepository.save(entry);
        FeocListEntryResponse response = toResponse(entry);
        auditService.record(authentication, "CREATE", "FeocListEntry", entry.getId(), null, response);
        return response;
    }

    @PutMapping("/{id}")
    public FeocListEntryResponse update(@PathVariable Long id, @Valid @RequestBody FeocListEntryRequest request, Authentication authentication) {
        FeocListEntry entry = getOrThrow(id);
        FeocListEntryResponse before = toResponse(entry);
        applyRequest(entry, request);
        entry = feocListEntryRepository.save(entry);
        FeocListEntryResponse after = toResponse(entry);
        auditService.record(authentication, "UPDATE", "FeocListEntry", entry.getId(), before, after);
        return after;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        FeocListEntry entry = getOrThrow(id);
        FeocListEntryResponse before = toResponse(entry);
        feocListEntryRepository.delete(entry);
        auditService.record(authentication, "DELETE", "FeocListEntry", id, before, null);
    }

    private void applyRequest(FeocListEntry entry, FeocListEntryRequest request) {
        entry.setEntryType(parseEnum(FeocListEntryType.class, request.entryType(), "entryType"));
        entry.setName(request.name());
        entry.setStatus(parseEnum(FeocListStatus.class, request.status(), "status"));
        entry.setNotes(request.notes());
        entry.setEffectiveFrom(request.effectiveFrom());
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String fieldName) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
        }
    }

    private FeocListEntry getOrThrow(Long id) {
        return feocListEntryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("FEOC list entry not found: " + id));
    }

    private FeocListEntryResponse toResponse(FeocListEntry entry) {
        return new FeocListEntryResponse(entry.getId(), entry.getEntryType().name(), entry.getName(),
            entry.getStatus().name(), entry.getNotes(), entry.getEffectiveFrom());
    }
}
