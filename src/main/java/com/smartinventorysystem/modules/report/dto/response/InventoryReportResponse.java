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
public class InventoryReportResponse {
    private InventoryReportSummary summary;
    private List<CategoryInventorySummary> categoryBreakdown;
    private MovementSummaryResponse movementSummary;
    private List<InventoryReportItem> items;
}
