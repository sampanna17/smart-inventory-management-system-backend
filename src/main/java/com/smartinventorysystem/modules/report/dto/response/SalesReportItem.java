package com.smartinventorysystem.modules.report.dto.response;

import com.smartinventorysystem.enums.SaleStatus;
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
public class SalesReportItem {
    private Integer saleId;
    private String invoiceNumber;
    private Integer customerId;
    private String customerName;
    private Integer userId;
    private String userName;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    private SaleStatus status;
    private int totalItems;
}
