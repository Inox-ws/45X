package com.inox.x45.web.controller;

import com.inox.x45.audit.AuditService;
import com.inox.x45.domain.CreditRate;
import com.inox.x45.domain.enums.ComponentType;
import com.inox.x45.repository.CreditRateRepository;
import com.inox.x45.web.dto.CreditRateRequest;
import com.inox.x45.web.dto.CreditRateResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Master Data - 45X credit rates (Section 5, Section 6.1). Admin-only.
 * Values here are the ONLY source the credit calculation engine (Milestone 6)
 * reads from - never hard-code a rate anywhere else.
 */
@RestController
@RequestMapping("/api/v1/master-data/credit-rates")
@PreAuthorize("hasRole('ADMIN')")
public class CreditRateController {

    private final CreditRateRepository creditRateRepository;
    private final AuditService auditService;

    public CreditRateController(CreditRateRepository creditRateRepository, AuditService auditService) {
        this.creditRateRepository = creditRateRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public List<CreditRateResponse> list(@RequestParam(required = false) String componentType) {
        List<CreditRate> rates = componentType == null || componentType.isBlank()
            ? creditRateRepository.findAll()
            : creditRateRepository.findByComponentTypeOrderByEffectiveFromDesc(parseComponentType(componentType));
        return rates.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public CreditRateResponse get(@PathVariable Long id) {
        return toResponse(getOrThrow(id));
    }

    @PostMapping
    public CreditRateResponse create(@Valid @RequestBody CreditRateRequest request, Authentication authentication) {
        validateRange(request);
        ComponentType componentType = parseComponentType(request.componentType());
        assertNoOverlap(componentType, request, null);

        CreditRate rate = new CreditRate();
        rate.setComponentType(componentType);
        rate.setRatePerWatt(request.ratePerWatt());
        rate.setEffectiveFrom(request.effectiveFrom());
        rate.setEffectiveTo(request.effectiveTo());
        rate = creditRateRepository.save(rate);

        CreditRateResponse response = toResponse(rate);
        auditService.record(authentication, "CREATE", "CreditRate", rate.getId(), null, response);
        return response;
    }

    @PutMapping("/{id}")
    public CreditRateResponse update(@PathVariable Long id, @Valid @RequestBody CreditRateRequest request, Authentication authentication) {
        validateRange(request);
        ComponentType componentType = parseComponentType(request.componentType());
        assertNoOverlap(componentType, request, id);

        CreditRate rate = getOrThrow(id);
        CreditRateResponse before = toResponse(rate);
        rate.setComponentType(componentType);
        rate.setRatePerWatt(request.ratePerWatt());
        rate.setEffectiveFrom(request.effectiveFrom());
        rate.setEffectiveTo(request.effectiveTo());
        rate = creditRateRepository.save(rate);

        CreditRateResponse after = toResponse(rate);
        auditService.record(authentication, "UPDATE", "CreditRate", rate.getId(), before, after);
        return after;
    }

    private void validateRange(CreditRateRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new IllegalArgumentException("effectiveTo must not be before effectiveFrom.");
        }
    }

    /** Two rates for the same component type must never both be in force on the same date (Section 6.1). */
    private void assertNoOverlap(ComponentType componentType, CreditRateRequest request, Long excludingId) {
        LocalDate newFrom = request.effectiveFrom();
        LocalDate newTo = request.effectiveTo();

        for (CreditRate existing : creditRateRepository.findByComponentTypeOrderByEffectiveFromDesc(componentType)) {
            if (existing.getId().equals(excludingId)) {
                continue;
            }
            LocalDate existingFrom = existing.getEffectiveFrom();
            LocalDate existingTo = existing.getEffectiveTo();

            boolean startsBeforeExistingEnds = existingTo == null || !newFrom.isAfter(existingTo);
            boolean endsAfterExistingStarts = newTo == null || !newTo.isBefore(existingFrom);
            if (startsBeforeExistingEnds && endsAfterExistingStarts) {
                throw new IllegalArgumentException(
                    "This date range overlaps an existing " + componentType + " rate (id " + existing.getId() + ").");
            }
        }
    }

    private ComponentType parseComponentType(String value) {
        try {
            return ComponentType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid componentType: " + value);
        }
    }

    private CreditRate getOrThrow(Long id) {
        return creditRateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Credit rate not found: " + id));
    }

    private CreditRateResponse toResponse(CreditRate rate) {
        return new CreditRateResponse(rate.getId(), rate.getComponentType().name(), rate.getRatePerWatt(),
            rate.getEffectiveFrom(), rate.getEffectiveTo());
    }
}
