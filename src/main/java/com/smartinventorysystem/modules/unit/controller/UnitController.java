package com.smartinventorysystem.modules.unit.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.modules.unit.dto.request.CreateUnitRequest;
import com.smartinventorysystem.modules.unit.dto.request.UnitFilterRequest;
import com.smartinventorysystem.modules.unit.dto.request.UpdateUnitRequest;
import com.smartinventorysystem.modules.unit.dto.response.UnitResponse;
import com.smartinventorysystem.modules.unit.service.UnitService;
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
@RequestMapping(ApiRoutes.Units.BASE)
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Units.CREATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(
            @Valid @RequestBody CreateUnitRequest request) {

        UnitResponse response = unitService.createUnit(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UnitResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Unit created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PutMapping(ApiRoutes.Units.UPDATE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UnitResponse>> updateUnit(
            @PathVariable Integer unitId,
            @RequestBody UpdateUnitRequest request) {

        UnitResponse response = unitService.updateUnit(unitId, request);

        return ResponseEntity.ok(
                ApiResponse.<UnitResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Unit updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @DeleteMapping(ApiRoutes.Units.DELETE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable Integer unitId) {

        unitService.deleteUnit(unitId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Unit deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Units.GET_BY_ID)
    public ResponseEntity<ApiResponse<UnitResponse>> getById(@PathVariable Integer unitId) {

        UnitResponse response = unitService.getUnitById(unitId);

        return ResponseEntity.ok(
                ApiResponse.<UnitResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Unit fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Units.GET_ALL)
    public ResponseEntity<ApiResponse<PageResponse<UnitResponse>>> getUnits(
            @Valid @ModelAttribute UnitFilterRequest filterRequest) {

        PageResponse<UnitResponse> response = unitService.getUnits(filterRequest);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<UnitResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Units fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UnitResponse>>> getAllUnits() {

        List<UnitResponse> response = unitService.getAllUnits();

        return ResponseEntity.ok(
                ApiResponse.<List<UnitResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("All units fetched successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}