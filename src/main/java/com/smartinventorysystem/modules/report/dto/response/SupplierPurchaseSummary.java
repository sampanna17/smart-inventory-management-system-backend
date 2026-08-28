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
public class SupplierPurchaseSummary {
    private Integer supplierId;
    private String supplierName;
    private String contactPerson;
    private long purchaseCount;
    private BigDecimal totalAmount;
    private long receivedCount;
}
