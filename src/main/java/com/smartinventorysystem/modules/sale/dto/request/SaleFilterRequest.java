package com.smartinventorysystem.modules.sale.dto.request;

import com.smartinventorysystem.enums.SaleStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleFilterRequest {

    private String search;
    private String invoiceNumber;
    private Integer customerId;
    private SaleStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    @Builder.Default
    @Min(value = 0, message = "Page index cannot be negative")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "saleDate";

    @Builder.Default
    private String sortDir = "desc";

    public Integer getPage() {
        return (page == null || page < 0) ? 0 : page;
    }

    public Integer getSize() {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    public String getSortBy() {
        return (sortBy == null || sortBy.isBlank()) ? "saleDate" : sortBy.trim();
    }

    public String getSortDir() {
        return (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir.trim();
    }
}
