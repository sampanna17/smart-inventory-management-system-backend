package com.smartinventorysystem.modules.sale.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.enums.SaleStatus;
import com.smartinventorysystem.modules.sale.dto.request.CreateSaleRequest;
import com.smartinventorysystem.modules.sale.dto.request.SaleFilterRequest;
import com.smartinventorysystem.modules.sale.dto.request.UpdateSaleRequest;
import com.smartinventorysystem.modules.sale.dto.request.UpdateSaleStatusRequest;
import com.smartinventorysystem.modules.sale.dto.response.SaleResponse;
import com.smartinventorysystem.modules.sale.dto.response.SaleSummaryResponse;
import com.smartinventorysystem.modules.sale.service.SaleService;
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
@RequestMapping(ApiRoutes.Sales.BASE)
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Sales.CREATE)
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(
            @Valid @RequestBody CreateSaleRequest request) {

        SaleResponse response = saleService.createSale(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SaleResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message(MessageConstants.SALE_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PutMapping(ApiRoutes.Sales.UPDATE)
    public ResponseEntity<ApiResponse<SaleResponse>> updateSale(
            @PathVariable Integer saleId,
            @Valid @RequestBody UpdateSaleRequest request) {

        SaleResponse response = saleService.updateSale(saleId, request);

        return ResponseEntity.ok(
                ApiResponse.<SaleResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Sale updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PatchMapping(ApiRoutes.Sales.UPDATE_STATUS)
    public ResponseEntity<ApiResponse<SaleResponse>> updateSaleStatus(
            @PathVariable Integer saleId,
            @Valid @RequestBody UpdateSaleStatusRequest request) {

        SaleResponse response = saleService.updateStatus(saleId, request);

        return ResponseEntity.ok(
                ApiResponse.<SaleResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Sale status updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @DeleteMapping(ApiRoutes.Sales.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSale(
            @PathVariable Integer saleId) {

        saleService.deleteSale(saleId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Sale deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Sales.GET_BY_ID)
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleById(
            @PathVariable Integer saleId) {

        SaleResponse response = saleService.getSale(saleId);

        return ResponseEntity.ok(
                ApiResponse.<SaleResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.SALE_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Sales.GET_ALL)
    public ResponseEntity<ApiResponse<PageResponse<SaleSummaryResponse>>> getSales(
            @Valid @ModelAttribute SaleFilterRequest filterRequest) {

        PageResponse<SaleSummaryResponse> response = saleService.getSales(filterRequest);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<SaleSummaryResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.SALE_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Sales.GET_BY_CUSTOMER)
    public ResponseEntity<ApiResponse<List<SaleSummaryResponse>>> getSalesByCustomer(
            @PathVariable Integer customerId) {

        List<SaleSummaryResponse> response = saleService.getSalesByCustomer(customerId);

        return ResponseEntity.ok(
                ApiResponse.<List<SaleSummaryResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.SALE_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Sales.GET_BY_STATUS)
    public ResponseEntity<ApiResponse<List<SaleSummaryResponse>>> getSalesByStatus(
            @PathVariable SaleStatus status) {

        List<SaleSummaryResponse> response = saleService.getSalesByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.<List<SaleSummaryResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.SALE_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}