package com.smartinventorysystem.modules.dashboard.dto.response;

import com.smartinventorysystem.modules.purchase.dto.response.PurchaseResponse;
import com.smartinventorysystem.modules.sale.dto.response.SaleSummaryResponse;
import com.smartinventorysystem.modules.stockmovement.dto.response.StockMovementResponse;
import lombok.Data;

import java.util.List;

@Data
public class DashboardRecentActivitiesResponse {
    private List<SaleSummaryResponse> recentSales;
    private List<PurchaseResponse> recentPurchases;
    private List<StockMovementResponse> recentStockMovements;
}
