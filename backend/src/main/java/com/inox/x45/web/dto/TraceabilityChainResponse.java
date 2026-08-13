package com.inox.x45.web.dto;

import java.util.List;

/** The Supplier -> Cell -> Module -> Invoice -> Customer chain (Section 6.3), from whichever node was searched. */
public record TraceabilityChainResponse(
    List<ModuleSummaryResponse> modules,
    List<TraceabilityInvoiceSummaryResponse> invoices
) {}
