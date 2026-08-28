package com.smartinventorysystem.modules.report.controller;

import com.smartinventorysystem.common.dto.ApiResponse;
import com.smartinventorysystem.constants.ApiRoutes;
import com.smartinventorysystem.modules.report.dto.request.AnalyticsFilterRequest;
import com.smartinventorysystem.modules.report.dto.request.InventoryReportFilterRequest;
import com.smartinventorysystem.modules.report.dto.request.PurchaseReportFilterRequest;
import com.smartinventorysystem.modules.report.dto.request.SalesReportFilterRequest;
import com.smartinventorysystem.modules.report.dto.response.*;
import com.smartinventorysystem.modules.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiRoutes.Reports.BASE)
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final Clock clock;

    @GetMapping(ApiRoutes.Reports.SALES)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<SalesReportResponse>> getSalesReport(
            @Valid @ModelAttribute SalesReportFilterRequest filter) {

        SalesReportResponse response = reportService.generateSalesReport(filter);

        return ResponseEntity.ok(
                ApiResponse.<SalesReportResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Sales report generated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Reports.INVENTORY)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<InventoryReportResponse>> getInventoryReport(
            @Valid @ModelAttribute InventoryReportFilterRequest filter) {

        InventoryReportResponse response = reportService.generateInventoryReport(filter);

        return ResponseEntity.ok(
                ApiResponse.<InventoryReportResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Inventory report generated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Reports.PURCHASES)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseReportResponse>> getPurchaseReport(
            @Valid @ModelAttribute PurchaseReportFilterRequest filter) {

        PurchaseReportResponse response = reportService.generatePurchaseReport(filter);

        return ResponseEntity.ok(
                ApiResponse.<PurchaseReportResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Purchase report generated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Reports.ANALYTICS_PRODUCTS)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductAnalyticsResponse>> getProductAnalytics(
            @Valid @ModelAttribute AnalyticsFilterRequest filter) {

        ProductAnalyticsResponse response = reportService.generateProductAnalytics(filter);

        return ResponseEntity.ok(
                ApiResponse.<ProductAnalyticsResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product analytics generated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Reports.ANALYTICS_CUSTOMERS)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerAnalyticsResponse>> getCustomerAnalytics(
            @Valid @ModelAttribute AnalyticsFilterRequest filter) {

        CustomerAnalyticsResponse response = reportService.generateCustomerAnalytics(filter);

        return ResponseEntity.ok(
                ApiResponse.<CustomerAnalyticsResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Customer analytics generated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Reports.STAFF)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffPerformanceReportResponse>> getStaffReport(
            @Valid @ModelAttribute AnalyticsFilterRequest filter) {

        StaffPerformanceReportResponse response = reportService.generateStaffPerformanceReport(filter);

        return ResponseEntity.ok(
                ApiResponse.<StaffPerformanceReportResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Staff performance report generated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @GetMapping(ApiRoutes.Reports.EXPORT_SALES)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<byte[]> exportSalesReport(
            @Valid @ModelAttribute SalesReportFilterRequest filter) {

        byte[] csv = reportService.exportSalesReportCsv(filter);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(ApiRoutes.Reports.EXPORT_INVENTORY)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<byte[]> exportInventoryReport(
            @Valid @ModelAttribute InventoryReportFilterRequest filter) {

        byte[] csv = reportService.exportInventoryReportCsv(filter);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(ApiRoutes.Reports.EXPORT_PURCHASES)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportPurchaseReport(
            @Valid @ModelAttribute PurchaseReportFilterRequest filter) {

        byte[] csv = reportService.exportPurchaseReportCsv(filter);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=purchase-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
