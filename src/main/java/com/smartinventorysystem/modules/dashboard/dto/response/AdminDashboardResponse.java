package com.smartinventorysystem.modules.dashboard.dto.response;
import lombok.Data;
import java.util.List;
@Data
public class AdminDashboardResponse {
    private AdminDashboardSummaryResponse summary;
    private DashboardChartsResponse charts;
    private List<TopSellingProductResponse> topSellingProducts;
    private DashboardRecentActivitiesResponse recentActivities;
}