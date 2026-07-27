package com.smartinventorysystem.modules.dashboard.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrendDataPointResponse {
    private String period;
    private Long count;
    private BigDecimal amount;
}
