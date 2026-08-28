package com.smartinventorysystem.modules.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReportResponse {
    private PurchaseReportSummary summary;
    private List<SupplierPurchaseSummary> supplierBreakdown;
    private List<PurchaseReportItem> purchases;
}
