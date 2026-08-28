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
public class InventoryReportItem {
    private Integer productId;
    private String productName;
    private Integer categoryId;
    private String categoryName;
    private String unitName;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private BigDecimal totalCostValue;
    private BigDecimal totalRetailValue;
    private BigDecimal potentialProfit;
    private String stockStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
}
