package com.smartinventorysystem.modules.customer.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.modules.customer.dto.request.CreateCustomerRequest;
import com.smartinventorysystem.modules.customer.dto.request.CustomerFilterRequest;
import com.smartinventorysystem.modules.customer.dto.request.UpdateCustomerRequest;
import com.smartinventorysystem.modules.customer.dto.response.CustomerResponse;
import com.smartinventorysystem.modules.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(ApiRoutes.Customers.BASE)
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Customers.CREATE)
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {

        CustomerResponse response = customerService.createCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CustomerResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Customer created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PutMapping(ApiRoutes.Customers.UPDATE)
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Integer customerId,
            @RequestBody UpdateCustomerRequest request) {

        CustomerResponse response = customerService.updateCustomer(customerId, request);

        return ResponseEntity.ok(
                ApiResponse.<CustomerResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Customer updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @DeleteMapping(ApiRoutes.Customers.DELETE)
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Integer customerId) {

        customerService.deleteCustomer(customerId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Customer deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Customers.GET_BY_ID)
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Integer customerId) {

        CustomerResponse response = customerService.getCustomerById(customerId);

        return ResponseEntity.ok(
                ApiResponse.<CustomerResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Customer fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Customers.GET_ALL)
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> getCustomers(
            @Valid @ModelAttribute CustomerFilterRequest filterRequest) {

        PageResponse<CustomerResponse> response = customerService.getCustomers(filterRequest);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<CustomerResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Customers fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {

        List<CustomerResponse> response = customerService.getAllCustomers();

        return ResponseEntity.ok(
                ApiResponse.<List<CustomerResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("All customers fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}