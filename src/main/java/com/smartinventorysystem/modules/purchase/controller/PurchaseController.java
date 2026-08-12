package com.smartinventorysystem.modules.purchase.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.modules.purchase.dto.request.CreatePurchaseRequest;
import com.smartinventorysystem.modules.purchase.dto.request.PurchaseFilterRequest;
import com.smartinventorysystem.modules.purchase.dto.request.UpdatePurchaseRequest;
import com.smartinventorysystem.modules.purchase.dto.request.UpdatePurchaseStatusRequest;
import com.smartinventorysystem.modules.purchase.dto.response.PurchaseResponse;
import com.smartinventorysystem.modules.purchase.service.PurchaseService;
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
@RequestMapping(ApiRoutes.Purchases.BASE)
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Purchases.CREATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseResponse>> createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request) {

        PurchaseResponse response = purchaseService.createPurchase(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<PurchaseResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Purchase created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PutMapping(ApiRoutes.Purchases.UPDATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseResponse>> updatePurchase(
            @PathVariable Integer purchaseId,
            @Valid @RequestBody UpdatePurchaseRequest request) {

        PurchaseResponse response = purchaseService.updatePurchase(purchaseId, request);

        return ResponseEntity.ok(
                ApiResponse.<PurchaseResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Purchase updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @DeleteMapping(ApiRoutes.Purchases.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePurchase(
            @PathVariable Integer purchaseId) {

        purchaseService.deletePurchase(purchaseId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Purchase deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Purchases.GET_BY_ID)
    public ResponseEntity<ApiResponse<PurchaseResponse>> getPurchaseById(
            @PathVariable Integer purchaseId) {

        PurchaseResponse response = purchaseService.getPurchaseById(purchaseId);

        return ResponseEntity.ok(
                ApiResponse.<PurchaseResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Purchase fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Purchases.GET_ALL)
    public ResponseEntity<ApiResponse<PageResponse<PurchaseResponse>>> getPurchases(
            @Valid @ModelAttribute PurchaseFilterRequest filterRequest) {

        PageResponse<PurchaseResponse> response = purchaseService.getPurchases(filterRequest);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<PurchaseResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Purchases fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PatchMapping(ApiRoutes.Purchases.UPDATE_STATUS)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseResponse>> updatePurchaseStatus(
            @PathVariable Integer purchaseId,
            @Valid @RequestBody UpdatePurchaseStatusRequest request) {

        PurchaseResponse response = purchaseService.updatePurchaseStatus(purchaseId, request);

        return ResponseEntity.ok(
                ApiResponse.<PurchaseResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Purchase status updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Purchases.GET_BY_SUPPLIER)
    public ResponseEntity<ApiResponse<List<PurchaseResponse>>> getPurchasesBySupplier(
            @PathVariable Integer supplierId) {

        List<PurchaseResponse> response = purchaseService.getPurchasesBySupplier(supplierId);

        return ResponseEntity.ok(
                ApiResponse.<List<PurchaseResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Purchases fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}
