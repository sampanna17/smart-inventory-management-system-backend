package com.smartinventorysystem.modules.report.dto.request;

import com.smartinventorysystem.enums.SaleStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class SalesReportFilterRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private Integer customerId;

    private Integer userId;

    private SaleStatus status;

    private String groupBy = "DAY"; // DAY, MONTH, YEAR
}
