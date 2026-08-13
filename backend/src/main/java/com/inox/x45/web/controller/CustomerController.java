package com.inox.x45.web.controller;

import com.inox.x45.domain.Customer;
import com.inox.x45.repository.CustomerRepository;
import com.inox.x45.web.dto.CustomerSummaryResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Minimal read-only lookup so Upload Invoice (Milestone 4) can attach a
 * customer. Full CRUD + search lands in Milestone 5's Master Data module.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCE','MANAGEMENT','ADMIN')")
    public List<CustomerSummaryResponse> list() {
        return customerRepository.findAll(PageRequest.of(0, 200)).stream()
            .map(c -> new CustomerSummaryResponse(c.getId(), c.getName()))
            .toList();
    }
}
