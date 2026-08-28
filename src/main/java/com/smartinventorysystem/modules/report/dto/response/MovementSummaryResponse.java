package com.smartinventorysystem.modules.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementSummaryResponse {
    private long totalPurchasedQty;
    private long totalSoldQty;
    private long totalAdjustedQty;
    private long totalReturnedQty;
}
