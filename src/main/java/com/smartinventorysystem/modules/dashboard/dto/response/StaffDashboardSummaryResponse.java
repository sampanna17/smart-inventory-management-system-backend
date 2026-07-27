package com.smartinventorysystem.modules.dashboard.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StaffDashboardSummaryResponse {
    private Long todaySales;
    private BigDecimal todayRevenue;
    private Long productsSoldToday;
    private Long lowStockProducts;
}
