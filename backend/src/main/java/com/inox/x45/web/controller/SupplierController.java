package com.inox.x45.web.controller;

import com.inox.x45.audit.AuditService;
import com.inox.x45.domain.Supplier;
import com.inox.x45.domain.enums.FeocStatus;
import com.inox.x45.repository.SupplierRepository;
import com.inox.x45.web.dto.PageResponse;
import com.inox.x45.web.dto.SupplierRequest;
import com.inox.x45.web.dto.SupplierResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

/** Master Data - Suppliers (Section 5). Admin-only, effective-dated FEOC status lives on the entity itself. */
@RestController
@RequestMapping("/api/v1/master-data/suppliers")
@PreAuthorize("hasRole('ADMIN')")
public class SupplierController {

    private final SupplierRepository supplierRepository;
    private final AuditService auditService;

    public SupplierController(SupplierRepository supplierRepository, AuditService auditService) {
        this.supplierRepository = supplierRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public PageResponse<SupplierResponse> list(@RequestParam(required = false) String name,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        var result = (name == null || name.isBlank())
            ? supplierRepository.findAll(pageable)
            : supplierRepository.findByNameContainingIgnoreCase(name, pageable);
        return PageResponse.of(result.map(this::toResponse));
    }

    @GetMapping("/{id}")
    public SupplierResponse get(@PathVariable Long id) {
        return toResponse(getOrThrow(id));
    }

    @PostMapping
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request, Authentication authentication) {
        Supplier supplier = new Supplier();
        applyRequest(supplier, request);
        supplier = supplierRepository.save(supplier);
        SupplierResponse response = toResponse(supplier);
        auditService.record(authentication, "CREATE", "Supplier", supplier.getId(), null, response);
        return response;
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request, Authentication authentication) {
        Supplier supplier = getOrThrow(id);
        SupplierResponse before = toResponse(supplier);
        applyRequest(supplier, request);
        supplier = supplierRepository.save(supplier);
        SupplierResponse after = toResponse(supplier);
        auditService.record(authentication, "UPDATE", "Supplier", supplier.getId(), before, after);
        return after;
    }

    private void applyRequest(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.name());
        supplier.setCountryOfOrigin(request.countryOfOrigin());
        supplier.setFeocStatus(parseFeocStatus(request.feocStatus()));
        supplier.setFeocNotes(request.feocNotes());
        supplier.setMaterialInfo(request.materialInfo());
        supplier.setActive(request.active());
    }

    private FeocStatus parseFeocStatus(String value) {
        if (value == null || value.isBlank()) {
            return FeocStatus.NEEDS_REVIEW;
        }
        try {
            return FeocStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid feocStatus: " + value);
        }
    }

    private Supplier getOrThrow(Long id) {
        return supplierRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + id));
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getName(), supplier.getCountryOfOrigin(),
            supplier.getFeocStatus().name(), supplier.getFeocNotes(), supplier.getMaterialInfo(), supplier.isActive());
    }
}
