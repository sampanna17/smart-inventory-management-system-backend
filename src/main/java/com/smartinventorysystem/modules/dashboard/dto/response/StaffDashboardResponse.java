package com.smartinventorysystem.modules.dashboard.dto.response;

import com.smartinventorysystem.modules.notification.dto.response.NotificationResponse;
import com.smartinventorysystem.modules.sale.dto.response.SaleSummaryResponse;
import lombok.Data;

import java.util.List;

@Data
public class StaffDashboardResponse {

    private StaffDashboardSummaryResponse summary;
    private List<SaleSummaryResponse> recentSales;
    private List<NotificationResponse> notifications;
}