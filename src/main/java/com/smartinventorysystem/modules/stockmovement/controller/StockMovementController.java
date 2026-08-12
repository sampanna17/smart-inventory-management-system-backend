package com.smartinventorysystem.modules.stockmovement.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.enums.MovementType;
import com.smartinventorysystem.modules.stockmovement.dto.request.CreateStockMovementRequest;
import com.smartinventorysystem.modules.stockmovement.dto.request.StockMovementFilterRequest;
import com.smartinventorysystem.modules.stockmovement.dto.response.StockMovementResponse;
import com.smartinventorysystem.modules.stockmovement.service.StockMovementService;
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
@RequestMapping(ApiRoutes.StockMovements.BASE)
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final Clock clock;

    @PostMapping(ApiRoutes.StockMovements.CREATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StockMovementResponse>> createStockMovement(
            @Valid @RequestBody CreateStockMovementRequest request) {

        StockMovementResponse response = stockMovementService.createStockMovement(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<StockMovementResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Stock movement created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.StockMovements.GET_BY_ID)
    public ResponseEntity<ApiResponse<StockMovementResponse>> getStockMovementById(
            @PathVariable Integer movementId) {

        StockMovementResponse response = stockMovementService.getStockMovementById(movementId);

        return ResponseEntity.ok(
                ApiResponse.<StockMovementResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.STOCK_MOVEMENT_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.StockMovements.GET_ALL)
    public ResponseEntity<ApiResponse<PageResponse<StockMovementResponse>>> getStockMovements(
            @Valid @ModelAttribute StockMovementFilterRequest filterRequest) {

        PageResponse<StockMovementResponse> response = stockMovementService.getStockMovements(filterRequest);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<StockMovementResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.STOCK_MOVEMENT_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getAllStockMovements() {

        List<StockMovementResponse> response = stockMovementService.getAllStockMovements();

        return ResponseEntity.ok(
                ApiResponse.<List<StockMovementResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.STOCK_MOVEMENT_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.StockMovements.GET_BY_PRODUCT)
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getMovementsByProduct(
            @PathVariable Integer productId) {

        List<StockMovementResponse> response = stockMovementService.getMovementsByProduct(productId);

        return ResponseEntity.ok(
                ApiResponse.<List<StockMovementResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.STOCK_MOVEMENT_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.StockMovements.GET_BY_USER)
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getMovementsByUser(
            @PathVariable Integer userId) {

        List<StockMovementResponse> response = stockMovementService.getMovementsByUser(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<StockMovementResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.STOCK_MOVEMENT_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.StockMovements.GET_BY_TYPE)
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getMovementsByMovementType(
            @PathVariable MovementType movementType) {

        List<StockMovementResponse> response = stockMovementService.getMovementsByMovementType(movementType.name());

        return ResponseEntity.ok(
                ApiResponse.<List<StockMovementResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.STOCK_MOVEMENT_FETCHED)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @DeleteMapping(ApiRoutes.StockMovements.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStockMovement(
            @PathVariable Integer movementId) {

        stockMovementService.deleteStockMovement(movementId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Stock movement deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}