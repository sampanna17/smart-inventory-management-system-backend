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
public class SalesReportSummary {
    private BigDecimal totalRevenue;
    private long totalSalesCount;
    private long totalUnitsSold;
    private BigDecimal averageOrderValue;
    private long completedSalesCount;
    private long refundedSalesCount;
    private long cancelledSalesCount;
}
