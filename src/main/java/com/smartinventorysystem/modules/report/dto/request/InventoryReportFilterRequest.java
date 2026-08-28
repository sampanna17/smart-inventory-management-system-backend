package com.smartinventorysystem.modules.report.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class InventoryReportFilterRequest {

    private Integer categoryId;

    private String stockStatus; // ALL, IN_STOCK, LOW_STOCK, OUT_OF_STOCK

    private String search;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime movementStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime movementEndDate;
}
