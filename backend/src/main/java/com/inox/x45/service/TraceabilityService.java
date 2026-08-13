package com.inox.x45.service;

import com.inox.x45.domain.Cell;
import com.inox.x45.domain.Invoice;
import com.inox.x45.domain.InvoiceModule;
import com.inox.x45.domain.Module;
import com.inox.x45.domain.ModuleCell;
import com.inox.x45.domain.Supplier;
import com.inox.x45.repository.CellRepository;
import com.inox.x45.repository.InvoiceModuleRepository;
import com.inox.x45.repository.InvoiceRepository;
import com.inox.x45.repository.ModuleCellRepository;
import com.inox.x45.repository.ModuleRepository;
import com.inox.x45.web.dto.CellSummaryResponse;
import com.inox.x45.web.dto.ModuleSummaryResponse;
import com.inox.x45.web.dto.SupplierSummaryResponse;
import com.inox.x45.web.dto.TraceabilityChainResponse;
import com.inox.x45.web.dto.TraceabilityInvoiceSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/** Assembles the Supplier -> Cell -> Module -> Invoice -> Customer chain (Section 6.3) from any anchor point. */
@Service
public class TraceabilityService {

    private final CellRepository cellRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleCellRepository moduleCellRepository;
    private final InvoiceModuleRepository invoiceModuleRepository;
    private final InvoiceRepository invoiceRepository;

    public TraceabilityService(CellRepository cellRepository, ModuleRepository moduleRepository,
                                ModuleCellRepository moduleCellRepository, InvoiceModuleRepository invoiceModuleRepository,
                                InvoiceRepository invoiceRepository) {
        this.cellRepository = cellRepository;
        this.moduleRepository = moduleRepository;
        this.moduleCellRepository = moduleCellRepository;
        this.invoiceModuleRepository = invoiceModuleRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public TraceabilityChainResponse byCellSerialNumber(String serialNumber) {
        Cell cell = cellRepository.findByCellSerialNumber(serialNumber)
            .orElseThrow(() -> new IllegalArgumentException("Cell not found: " + serialNumber));
        Module module = moduleCellRepository.findByCellId(cell.getId()).map(ModuleCell::getModule).orElse(null);
        if (module == null) {
            return new TraceabilityChainResponse(List.of(), List.of());
        }
        return chainFromModule(module);
    }

    public TraceabilityChainResponse byModuleSerialNumber(String serialNumber) {
        Module module = moduleRepository.findByModuleSerialNumber(serialNumber)
            .orElseThrow(() -> new IllegalArgumentException("Module not found: " + serialNumber));
        return chainFromModule(module);
    }

    public TraceabilityChainResponse byInvoiceNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceNumber));
        List<InvoiceModule> invoiceModules = invoiceModuleRepository.findByInvoiceId(invoice.getId());
        List<ModuleSummaryResponse> modules = invoiceModules.stream()
            .map(InvoiceModule::getModule)
            .map(this::toModuleSummary)
            .toList();
        return new TraceabilityChainResponse(modules, List.of(toInvoiceSummary(invoice)));
    }

    private TraceabilityChainResponse chainFromModule(Module module) {
        List<TraceabilityInvoiceSummaryResponse> invoices = invoiceModuleRepository.findByModuleId(module.getId()).stream()
            .map(InvoiceModule::getInvoice)
            .map(this::toInvoiceSummary)
            .toList();
        return new TraceabilityChainResponse(List.of(toModuleSummary(module)), invoices);
    }

    private ModuleSummaryResponse toModuleSummary(Module module) {
        List<CellSummaryResponse> cells = moduleCellRepository.findByModuleId(module.getId()).stream()
            .map(ModuleCell::getCell)
            .map(this::toCellSummary)
            .toList();
        return new ModuleSummaryResponse(module.getId(), module.getModuleSerialNumber(), module.getWattage(),
            module.getProductionDate(), cells);
    }

    private CellSummaryResponse toCellSummary(Cell cell) {
        return new CellSummaryResponse(cell.getId(), cell.getCellSerialNumber(), cell.getBatch(), cell.getLot(),
            cell.getWattage(), cell.getCountryOfOrigin(), cell.getFeocStatus().name(), toSupplierSummary(cell.getSupplier()));
    }

    private SupplierSummaryResponse toSupplierSummary(Supplier supplier) {
        return new SupplierSummaryResponse(supplier.getId(), supplier.getName(), supplier.getCountryOfOrigin(),
            supplier.getFeocStatus().name());
    }

    private TraceabilityInvoiceSummaryResponse toInvoiceSummary(Invoice invoice) {
        return new TraceabilityInvoiceSummaryResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getInvoiceDate(),
            invoice.getAmount(), invoice.getCurrency(), invoice.getStatus().name(), invoice.getCustomer().getName());
    }
}
