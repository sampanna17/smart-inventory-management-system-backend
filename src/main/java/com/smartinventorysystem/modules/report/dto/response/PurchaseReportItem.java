package com.smartinventorysystem.modules.report.dto.response;

import com.smartinventorysystem.enums.PurchaseStatus;
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
public class PurchaseReportItem {
    private Integer purchaseId;
    private String purchaseNumber;
    private Integer supplierId;
    private String supplierName;
    private Integer userId;
    private String userName;
    private LocalDateTime purchaseDate;
    private BigDecimal totalAmount;
    private PurchaseStatus status;
    private int totalItems;
}
