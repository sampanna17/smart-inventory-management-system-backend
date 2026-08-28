package com.smartinventorysystem.modules.report.dto.response;

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
public class CustomerPerformanceData {
    private Integer customerId;
    private String customerName;
    private String email;
    private String phone;
    private long totalOrders;
    private BigDecimal totalSpend;
    private BigDecimal averageOrderValue;
    private LocalDateTime lastOrderDate;
}
