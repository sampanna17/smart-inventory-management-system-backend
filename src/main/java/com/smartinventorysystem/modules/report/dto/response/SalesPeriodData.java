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
public class SalesPeriodData {
    private String period; // e.g. "2026-08-28", "2026-08", "2026"
    private long salesCount;
    private BigDecimal revenue;
    private long unitsSold;
}
