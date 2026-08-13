package com.inox.x45.web.controller;

import com.inox.x45.audit.AuditService;
import com.inox.x45.domain.Customer;
import com.inox.x45.repository.CustomerRepository;
import com.inox.x45.web.dto.CustomerRequest;
import com.inox.x45.web.dto.CustomerResponse;
import com.inox.x45.web.dto.PageResponse;
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

/** Master Data - Customers (Section 5). Admin-only; see CustomerController for the read-only dropdown lookup. */
@RestController
@RequestMapping("/api/v1/master-data/customers")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerMasterDataController {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public CustomerMasterDataController(CustomerRepository customerRepository, AuditService auditService) {
        this.customerRepository = customerRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public PageResponse<CustomerResponse> list(@RequestParam(required = false) String name,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        var result = (name == null || name.isBlank())
            ? customerRepository.findAll(pageable)
            : customerRepository.findByNameContainingIgnoreCase(name, pageable);
        return PageResponse.of(result.map(this::toResponse));
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return toResponse(getOrThrow(id));
    }

    @PostMapping
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request, Authentication authentication) {
        Customer customer = new Customer();
        applyRequest(customer, request);
        customer = customerRepository.save(customer);
        CustomerResponse response = toResponse(customer);
        auditService.record(authentication, "CREATE", "Customer", customer.getId(), null, response);
        return response;
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request, Authentication authentication) {
        Customer customer = getOrThrow(id);
        CustomerResponse before = toResponse(customer);
        applyRequest(customer, request);
        customer = customerRepository.save(customer);
        CustomerResponse after = toResponse(customer);
        auditService.record(authentication, "UPDATE", "Customer", customer.getId(), before, after);
        return after;
    }

    private void applyRequest(Customer customer, CustomerRequest request) {
        customer.setName(request.name());
        customer.setAddress(request.address());
        customer.setContactName(request.contactName());
        customer.setContactEmail(request.contactEmail());
        customer.setContactPhone(request.contactPhone());
        customer.setActive(request.active());
    }

    private Customer getOrThrow(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getAddress(),
            customer.getContactName(), customer.getContactEmail(), customer.getContactPhone(), customer.isActive());
    }
}
