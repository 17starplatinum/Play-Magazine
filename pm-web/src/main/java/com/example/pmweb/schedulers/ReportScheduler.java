package com.example.pmweb.schedulers;

import com.example.pmcommon.model.SalesReport;
import com.example.pmweb.services.data.PurchaseService;
import com.example.pmweb.services.data.ReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportScheduler {
    private final PurchaseService purchaseService;
    private final ReportService reportService;

    public ReportScheduler(PurchaseService purchaseService,
                           ReportService reportService) {
        this.purchaseService = purchaseService;
        this.reportService = reportService;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailySalesReport() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        SalesReport report = purchaseService.generateDailySalesReport(yesterday);
        reportService.saveReport(report);
        reportService.sendReportToStakeholders(report);
    }
}
