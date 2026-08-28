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
public class CategoryInventorySummary {
    private Integer categoryId;
    private String categoryName;
    private long productCount;
    private long totalUnitsInStock;
    private BigDecimal totalCostValue;
    private BigDecimal totalRetailValue;
}
