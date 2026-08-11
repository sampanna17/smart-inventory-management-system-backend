package com.smartinventorysystem.modules.supplier.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.modules.supplier.dto.request.CreateSupplierRequest;
import com.smartinventorysystem.modules.supplier.dto.request.SupplierFilterRequest;
import com.smartinventorysystem.modules.supplier.dto.request.UpdateSupplierRequest;
import com.smartinventorysystem.modules.supplier.dto.response.SupplierResponse;
import com.smartinventorysystem.modules.supplier.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(ApiRoutes.Suppliers.BASE)
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Suppliers.CREATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {

        SupplierResponse response = supplierService.createSupplier(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SupplierResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Supplier created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PutMapping(ApiRoutes.Suppliers.UPDATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @PathVariable Integer supplierId,
            @RequestBody UpdateSupplierRequest request) {

        SupplierResponse response = supplierService.updateSupplier(supplierId, request);

        return ResponseEntity.ok(
                ApiResponse.<SupplierResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Supplier updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @DeleteMapping(ApiRoutes.Suppliers.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Integer supplierId) {

        supplierService.deleteSupplier(supplierId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Supplier deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Suppliers.GET_BY_ID)
    public ResponseEntity<ApiResponse<SupplierResponse>> getById(@PathVariable Integer supplierId) {

        SupplierResponse response = supplierService.getSupplierById(supplierId);

        return ResponseEntity.ok(
                ApiResponse.<SupplierResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Supplier fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Suppliers.GET_ALL)
    public ResponseEntity<ApiResponse<PageResponse<SupplierResponse>>> getSuppliers(
            @Valid @ModelAttribute SupplierFilterRequest filterRequest) {

        PageResponse<SupplierResponse> response = supplierService.getSuppliers(filterRequest);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<SupplierResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Suppliers fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {

        List<SupplierResponse> response = supplierService.getAllSuppliers();

        return ResponseEntity.ok(
                ApiResponse.<List<SupplierResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("All suppliers fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}