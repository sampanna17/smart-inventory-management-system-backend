package com.smartinventorysystem.modules.report.dto.response;

import com.smartinventorysystem.enums.Role;
import com.smartinventorysystem.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPerformanceData {
    private Integer staffId;
    private String fullName;
    private String email;
    private Role role;
    private Status status;
    private long totalSalesCount;
    private BigDecimal totalRevenue;
    private long totalUnitsSold;
    private BigDecimal averageSaleValue;
    private LocalDateTime lastSaleDate;
}
