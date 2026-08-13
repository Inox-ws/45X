package com.inox.x45.web.controller;

import com.inox.x45.service.TraceabilityService;
import com.inox.x45.web.dto.TraceabilityChainResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Traceability (Section 5, module 5; Section 6.3): drill down from any node in the chain. */
@RestController
@RequestMapping("/api/v1/traceability")
@PreAuthorize("hasAnyRole('PRODUCTION','MANAGEMENT','ADMIN')")
public class TraceabilityController {

    private final TraceabilityService traceabilityService;

    public TraceabilityController(TraceabilityService traceabilityService) {
        this.traceabilityService = traceabilityService;
    }

    @GetMapping("/cells/{serialNumber}")
    public TraceabilityChainResponse byCell(@PathVariable String serialNumber) {
        return traceabilityService.byCellSerialNumber(serialNumber);
    }

    @GetMapping("/modules/{serialNumber}")
    public TraceabilityChainResponse byModule(@PathVariable String serialNumber) {
        return traceabilityService.byModuleSerialNumber(serialNumber);
    }

    @GetMapping("/invoices/{invoiceNumber}")
    public TraceabilityChainResponse byInvoice(@PathVariable String invoiceNumber) {
        return traceabilityService.byInvoiceNumber(invoiceNumber);
    }
}
