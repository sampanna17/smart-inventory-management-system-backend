package com.smartinventorysystem.modules.stockmovement.dto.request;

import com.smartinventorysystem.enums.MovementType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementFilterRequest {

    private String search;
    private Integer productId;
    private Integer userId;
    private MovementType movementType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private Integer minQuantity;
    private Integer maxQuantity;

    @Builder.Default
    @Min(value = 0, message = "Page index cannot be negative")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "movementDate";

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
        return (sortBy == null || sortBy.isBlank()) ? "movementDate" : sortBy.trim();
    }

    public String getSortDir() {
        return (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir.trim();
    }
}
