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
public class PurchaseReportSummary {
    private long totalPurchasesCount;
    private BigDecimal totalExpenditure;
    private long receivedCount;
    private long pendingCount;
    private long cancelledCount;
}
