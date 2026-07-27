package com.smartinventorysystem.modules.dashboard.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class DashboardChartsResponse {
    private List<TrendDataPointResponse> salesTrend;
    private List<TrendDataPointResponse> revenueTrend;
}
