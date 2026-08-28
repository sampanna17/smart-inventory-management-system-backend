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
public class ProductAnalyticsResponse {
    private List<ProductPerformanceData> topSellingByQuantity;
    private List<ProductPerformanceData> topSellingByRevenue;
    private List<CategoryPerformanceData> categoryPerformance;
}
