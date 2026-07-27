package com.smartinventorysystem.modules.dashboard.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopSellingProductResponse {
    private Integer productId;
    private String productName;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
}
