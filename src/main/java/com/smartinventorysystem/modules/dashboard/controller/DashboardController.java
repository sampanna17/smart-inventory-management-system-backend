package com.smartinventorysystem.modules.dashboard.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.modules.dashboard.dto.response.AdminDashboardResponse;
import com.smartinventorysystem.modules.dashboard.dto.response.StaffDashboardResponse;
import com.smartinventorysystem.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiRoutes.Dashboard.BASE)
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final Clock clock;

    @GetMapping(ApiRoutes.Dashboard.ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {

        AdminDashboardResponse response = dashboardService.getAdminDashboard();

        return ResponseEntity.ok(
                ApiResponse.<AdminDashboardResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.DASHBOARD_ADMIN_FETCH_SUCCESS)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Dashboard.STAFF)
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<StaffDashboardResponse>> getStaffDashboard() {

        StaffDashboardResponse response = dashboardService.getStaffDashboard();

        return ResponseEntity.ok(
                ApiResponse.<StaffDashboardResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message(MessageConstants.DASHBOARD_STAFF_FETCH_SUCCESS)
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}