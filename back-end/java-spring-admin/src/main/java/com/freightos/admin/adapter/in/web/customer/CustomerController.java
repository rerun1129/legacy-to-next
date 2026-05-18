package com.freightos.admin.adapter.in.web.customer;

import com.freightos.admin.adapter.in.web.customer.dto.CreateCustomerRequest;
import com.freightos.admin.adapter.in.web.customer.dto.CustomerDetailResponse;
import com.freightos.admin.adapter.in.web.customer.dto.CustomerSummaryResponse;
import com.freightos.admin.adapter.in.web.customer.dto.SearchCustomerRequest;
import com.freightos.admin.adapter.in.web.customer.dto.UpdateCustomerRequest;
import com.freightos.admin.application.customer.port.in.CustomerUseCase;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.customer.entity.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/customer")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN') or hasAuthority('CUSTOMER_MANAGE')")
public class CustomerController {

    private final CustomerUseCase customerUseCase;
    private final CustomerAssembler customerAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<CustomerSummaryResponse>>> search(
            @Valid @RequestBody SearchCustomerRequest req) {
        PagedResult<CustomerSummary> result = customerUseCase.searchCustomers(customerAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(customerAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getById(@PathVariable Long id) {
        Customer domain = customerUseCase.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.of(customerAssembler.toDetail(domain)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateCustomerRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = customerUseCase.createCustomer(customerAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/customer/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.CUSTOMER_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest req) {
        customerUseCase.updateCustomer(id, customerAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CUSTOMER_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        customerUseCase.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.CUSTOMER_DELETED.getMessage()));
    }
}
