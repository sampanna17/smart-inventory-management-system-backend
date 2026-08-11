package com.smartinventorysystem.modules.unit.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitFilterRequest {

    private String search;
    private String unitName;

    @Builder.Default
    @Min(value = 0, message = "Page index cannot be negative")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "unitId";

    @Builder.Default
    private String sortDir = "asc";

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
        return (sortBy == null || sortBy.isBlank()) ? "unitId" : sortBy.trim();
    }

    public String getSortDir() {
        return (sortDir == null || sortDir.isBlank()) ? "asc" : sortDir.trim();
    }
}
