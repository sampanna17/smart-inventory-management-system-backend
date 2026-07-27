package com.smartinventorysystem.modules.dashboard.dto.response;

import com.smartinventorysystem.enums.PurchaseStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseSummaryResponse {
    private Integer purchaseId;
    private String purchaseNumber;
    private Integer supplierId;
    private String supplierName;
    private Integer userId;
    private String userName;
    private LocalDateTime purchaseDate;
    private BigDecimal totalAmount;
    private PurchaseStatus status;
    private LocalDateTime createdAt;
}