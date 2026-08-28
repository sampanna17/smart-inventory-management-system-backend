package com.smartinventorysystem.modules.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportSummary {
    private long totalProductsCount;
    private long totalUnitsInStock;
    private BigDecimal totalCostValue;
    private BigDecimal totalRetailValue;
    private BigDecimal totalPotentialProfit;
    private long lowStockCount;
    private long outOfStockCount;
}
