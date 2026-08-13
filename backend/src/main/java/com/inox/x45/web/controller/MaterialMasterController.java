package com.inox.x45.web.controller;

import com.inox.x45.audit.AuditService;
import com.inox.x45.domain.MaterialMaster;
import com.inox.x45.repository.MaterialMasterRepository;
import com.inox.x45.web.dto.MaterialMasterRequest;
import com.inox.x45.web.dto.MaterialMasterResponse;
import com.inox.x45.web.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Master Data - Material Master (Section 5, Section 7). Normally synced from
 * SAP (Section 8, Milestone 8); manual CRUD here covers corrections and the
 * period before that sync exists.
 */
@RestController
@RequestMapping("/api/v1/master-data/materials")
@PreAuthorize("hasRole('ADMIN')")
public class MaterialMasterController {

    private final MaterialMasterRepository materialMasterRepository;
    private final AuditService auditService;

    public MaterialMasterController(MaterialMasterRepository materialMasterRepository, AuditService auditService) {
        this.materialMasterRepository = materialMasterRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public PageResponse<MaterialMasterResponse> list(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "50") int size) {
        var result = materialMasterRepository.findAll(PageRequest.of(page, size, Sort.by("materialCode")));
        return PageResponse.of(result.map(this::toResponse));
    }

    @GetMapping("/{id}")
    public MaterialMasterResponse get(@PathVariable Long id) {
        return toResponse(getOrThrow(id));
    }

    @PostMapping
    public MaterialMasterResponse create(@Valid @RequestBody MaterialMasterRequest request, Authentication authentication) {
        MaterialMaster material = new MaterialMaster();
        material.setMaterialCode(request.materialCode());
        material.setDescription(request.description());
        material.setUom(request.uom());
        material.setSource("MANUAL");
        material = materialMasterRepository.save(material);
        MaterialMasterResponse response = toResponse(material);
        auditService.record(authentication, "CREATE", "MaterialMaster", material.getId(), null, response);
        return response;
    }

    @PutMapping("/{id}")
    public MaterialMasterResponse update(@PathVariable Long id, @Valid @RequestBody MaterialMasterRequest request, Authentication authentication) {
        MaterialMaster material = getOrThrow(id);
        MaterialMasterResponse before = toResponse(material);
        material.setMaterialCode(request.materialCode());
        material.setDescription(request.description());
        material.setUom(request.uom());
        material = materialMasterRepository.save(material);
        MaterialMasterResponse after = toResponse(material);
        auditService.record(authentication, "UPDATE", "MaterialMaster", material.getId(), before, after);
        return after;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        MaterialMaster material = getOrThrow(id);
        MaterialMasterResponse before = toResponse(material);
        materialMasterRepository.delete(material);
        auditService.record(authentication, "DELETE", "MaterialMaster", id, before, null);
    }

    private MaterialMaster getOrThrow(Long id) {
        return materialMasterRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Material not found: " + id));
    }

    private MaterialMasterResponse toResponse(MaterialMaster material) {
        return new MaterialMasterResponse(material.getId(), material.getMaterialCode(), material.getDescription(),
            material.getUom(), material.getSource(), material.getLastSyncedAt());
    }
}
