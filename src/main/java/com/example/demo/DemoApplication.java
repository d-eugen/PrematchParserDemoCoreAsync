package com.example.demo;

import com.example.demo.exception.ApiException;
import com.example.demo.model.SportType;
import com.example.demo.model.report.ReportResult;
import com.example.demo.service.report.ReportPrintService;
import com.example.demo.service.report.AsyncReportService;
import com.example.demo.service.report.ReportService;
import com.example.demo.utils.PerformanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        //printTopLeaguesMarketReport();
        //printTopLeaguesMarketReportAsync();

        PerformanceUtils.measureRuntime(
                DemoApplication::printTopLeaguesMarketReport,
                "DemoApplication::printTopLeaguesMarketReport");

        PerformanceUtils.measureRuntime(
                DemoApplication::printTopLeaguesMarketReportAsync,
                "DemoApplication::printTopLeaguesMarketReportAsync");

    }

    public static void printTopLeaguesMarketReportAsync() {
        try {
            //AsyncTopLeagueMarketsReportService service = new AsyncTopLeagueMarketsReportService();
            AsyncReportService service = PerformanceUtils
                    .measureRuntime(AsyncReportService::new,
                            "AsyncTopLeagueMarketsReportService::new")
                    .orElseThrow();

            List<String> selectedSportNames = Arrays.stream(SportType.values())
                    .map(SportType::getDisplayName)
                    .toList();

            //CompletableFuture<ReportResult> reportFuture = service.generateReport();
            CompletableFuture<ReportResult> reportFuture = PerformanceUtils
                    .measureRuntime(() -> service.generateReportAsync(selectedSportNames),
                            "AsyncTopLeagueMarketsReportService::generateReport")
                    .orElseThrow();

            reportFuture.thenAccept(reportResult -> {
                        ReportPrintService printService = new ReportPrintService(reportResult);
                        //printService.printReport();
                    })
                    .thenRun(service::shutdown)
                    .join();

        } catch (ApiException e) {
            logger.error("Application encountered error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printTopLeaguesMarketReport() {
        try {
            ReportService reportService = new ReportService();
            List<String> selectedSportNames = Arrays.stream(SportType.values())
                    .map(SportType::getDisplayName)
                    .toList();
            ReportResult reportResult = reportService.generateReport(selectedSportNames);
            ReportPrintService printService = new ReportPrintService(reportResult);
            //printService.printReport();
        } catch (ApiException e) {
            logger.error("Application encountered error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
