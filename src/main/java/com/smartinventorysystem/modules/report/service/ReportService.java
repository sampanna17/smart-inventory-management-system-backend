package com.smartinventorysystem.modules.report.service;

import com.smartinventorysystem.modules.report.dto.request.AnalyticsFilterRequest;
import com.smartinventorysystem.modules.report.dto.request.InventoryReportFilterRequest;
import com.smartinventorysystem.modules.report.dto.request.PurchaseReportFilterRequest;
import com.smartinventorysystem.modules.report.dto.request.SalesReportFilterRequest;
import com.smartinventorysystem.modules.report.dto.response.*;

public interface ReportService {

    SalesReportResponse generateSalesReport(SalesReportFilterRequest filter);

    InventoryReportResponse generateInventoryReport(InventoryReportFilterRequest filter);

    PurchaseReportResponse generatePurchaseReport(PurchaseReportFilterRequest filter);

    ProductAnalyticsResponse generateProductAnalytics(AnalyticsFilterRequest filter);

    CustomerAnalyticsResponse generateCustomerAnalytics(AnalyticsFilterRequest filter);

    StaffPerformanceReportResponse generateStaffPerformanceReport(AnalyticsFilterRequest filter);

    byte[] exportSalesReportCsv(SalesReportFilterRequest filter);

    byte[] exportInventoryReportCsv(InventoryReportFilterRequest filter);

    byte[] exportPurchaseReportCsv(PurchaseReportFilterRequest filter);
}
