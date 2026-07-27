package com.smartinventorysystem.modules.dashboard.service;

import com.smartinventorysystem.modules.dashboard.dto.response.AdminDashboardResponse;
import com.smartinventorysystem.modules.dashboard.dto.response.StaffDashboardResponse;

public interface DashboardService {
    AdminDashboardResponse getAdminDashboard();
    StaffDashboardResponse getStaffDashboard();
}