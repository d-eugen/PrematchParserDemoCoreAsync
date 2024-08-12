package com.example.demo;

import com.example.demo.model.SportType;
import com.example.demo.model.report.ReportResult;
import com.example.demo.service.report.AsyncReportService;
import com.example.demo.service.report.ReportService;
import com.example.demo.utils.PerformanceUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class DemoApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(DemoApplicationTests.class);

    @Test
    void testMeasureSyncReportGenerationTime() {
        ReportService reportService = new ReportService();
        int retry = 3;
        PerformanceUtils.measureRuntime(() -> {
            List<String> selectedSportNames = Arrays.stream(SportType.values())
                    .map(SportType::getDisplayName)
                    .toList();
            int n = retry;
            while(n-- > 0){
                reportService.generateReport(selectedSportNames);
            }
        }, String.format("Synchronous data load x%s", retry));
    }

    @Test
    void testMeasureAsyncReportGenerationTime() {
        AsyncReportService asyncReportService = new AsyncReportService();
        int retry = 3;
        PerformanceUtils.measureRuntime(() -> {
            try {
                logger.info("Enter runnable task");
                List<String> selectedSportNames = Arrays.stream(SportType.values())
                        .map(SportType::getDisplayName)
                        .toList();
                logger.info("Number of sports: " + selectedSportNames.size());
                int n = retry;
                // Wait to finish previous batch not to spam API
                while (n-- > 0) {
                    logger.info("Retries left: " + n);
                    CompletableFuture<ReportResult> futureReport = asyncReportService.generateReportAsync(selectedSportNames);
                    logger.info("Before wait");
                    ReportResult report = futureReport.join();
                    logger.info("Wait complete");
                }
            } catch (Exception e) {
                logger.error("Error: " + e.getMessage(), e);
            } finally {
                asyncReportService.shutdown();
            }
        }, String.format("Asynchronous data load x%s", retry));
    }

    @Test
    void testAsyncReportGenerationTime_batch() {
        AsyncReportService asyncReportService = new AsyncReportService();
        int retry = 1;
        PerformanceUtils.measureRuntime(() -> {
            try {
                logger.info("Enter runnable task");
                List<String> selectedSportNames = Arrays.stream(SportType.values())
                        .map(SportType::getDisplayName)
                        .toList();
                logger.info("Number of sports: " + selectedSportNames.size());
                int n = retry;

                List<CompletableFuture<ReportResult>> futures = new ArrayList<>();

                while (n-- > 0) {
                    logger.info("Retries left: " + n);
                    CompletableFuture<ReportResult> futureReport = asyncReportService.generateReportAsync(selectedSportNames);
                    futures.add(futureReport);
                }

                CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

                allOf.thenAccept(x -> System.out.println("READY"))
                        .join();

            } catch (Exception e) {
                logger.error("Error: " + e.getMessage(), e);
            } finally {
                asyncReportService.shutdown();
            }
        }, String.format("Asynchronous data load x%s", retry));
    }
}
