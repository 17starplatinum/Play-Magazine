package com.example.pmcommon.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Data
public class SalesReport {
    private UUID id;
    private LocalDate reportDate;
    private Map<UUID, Integer> appSalesCount;
    private Map<UUID, Double> appRevenue;
    private Double totalRevenue;
    private Integer totalSales;
}
