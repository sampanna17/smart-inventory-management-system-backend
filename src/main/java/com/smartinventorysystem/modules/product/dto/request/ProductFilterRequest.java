package com.smartinventorysystem.modules.product.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    private String search;
    private Integer categoryId;
    private Integer unitId;
    private String stockStatus;

    @Min(value = 0, message = "Minimum price cannot be negative")
    private BigDecimal minPrice;

    @Min(value = 0, message = "Maximum price cannot be negative")
    private BigDecimal maxPrice;

    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minStock;

    @Min(value = 0, message = "Maximum stock cannot be negative")
    private Integer maxStock;

    @Builder.Default
    @Min(value = 0, message = "Page index cannot be negative")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

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
        return (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy.trim();
    }

    public String getSortDir() {
        return (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir.trim();
    }
}
