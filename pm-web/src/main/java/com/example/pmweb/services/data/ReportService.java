package com.example.pmweb.services.data;

import com.example.pmcommon.model.SalesReport;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public interface ReportService {
    void saveReport(SalesReport salesReport);
    void sendReportToStakeholders(SalesReport salesReport);
    SalesReport getSalesReport(UUID salesReportId);
    List<SalesReport> getReportsByDateRange(LocalDate startDate, LocalDate endDate);
}
