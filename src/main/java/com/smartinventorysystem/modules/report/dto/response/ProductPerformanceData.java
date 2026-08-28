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
public class ProductPerformanceData {
    private Integer productId;
    private String productName;
    private String categoryName;
    private long totalQuantitySold;
    private BigDecimal totalRevenue;
    private BigDecimal currentStock;
}
