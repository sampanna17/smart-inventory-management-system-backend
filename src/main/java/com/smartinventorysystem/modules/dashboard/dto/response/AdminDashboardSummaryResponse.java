package com.smartinventorysystem.modules.dashboard.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminDashboardSummaryResponse {
    private Long totalProducts;
    private Long totalCategories;
    private Long totalSuppliers;
    private Long totalCustomers;
    private Long totalSales;
    private BigDecimal totalRevenue;
    private Long totalPurchases;
    private Long lowStockProducts;
    private Long outOfStockProducts;
    private Long unreadNotifications;
}
