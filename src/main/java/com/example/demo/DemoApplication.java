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
        PerformanceUtils.measureRuntime(
                DemoApplication::printTopLeaguesMarketReportAsync,
                "DemoApplication::printTopLeaguesMarketReportAsync");
    }

    /**
     * Asynchronously generates and prints a report for top leagues and their market information.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *     <li>Initializes an {@link AsyncReportService}.</li>
     *     <li>Retrieves all sport names from the {@link SportType} enum.</li>
     *     <li>Asynchronously generates a report for all sports.</li>
     *     <li>Prints the generated report using {@link ReportPrintService}.</li>
     *     <li>Shuts down the {@link AsyncReportService} after completion.</li>
     * </ol>
     *
     * <p>The method uses {@link CompletableFuture} for asynchronous processing and handles
     * both {@link ApiException} and unexpected exceptions.</p>
     */
    public static void printTopLeaguesMarketReportAsync() {
        AsyncReportService service = new AsyncReportService();
        try {
            logger.info("Starting asynchronous report generation...");
            List<String> selectedSportNames = Arrays.stream(SportType.values())
                    .map(SportType::getDisplayName)
                    .toList();

            CompletableFuture<ReportResult> reportFuture = service.generateReportAsync(selectedSportNames);

            reportFuture.thenAccept(reportResult -> {
                        logger.info("Report generation completed. Starting report printing...");
                        ReportPrintService printService = new ReportPrintService(reportResult);
                        printService.printReport();
                    })
                    .join();
            logger.info("Report generation finished.");

        } catch (ApiException e) {
            logger.error("Application error: " + e.getMessage());
        } catch (Exception e) {
            if (e.getCause() instanceof ApiException) {
                logger.error("Application error: " + e.getMessage());
            } else {
                logger.error("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            service.shutdown();
        }
    }

    /**
     * Synchronously generates and prints a report for top leagues and their market information.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *     <li>Initializes a {@link ReportService}.</li>
     *     <li>Retrieves all sport names from the {@link SportType} enum.</li>
     *     <li>Generates a report for all sports using the {@link ReportService}.</li>
     *     <li>Prints the generated report using {@link ReportPrintService}.</li>
     * </ol>
     *
     * <p>The method executes synchronously and handles both {@link ApiException} and unexpected exceptions.
     * It logs the progress of report generation and printing.</p>
     */
    public static void printTopLeaguesMarketReport() {
        try {
            logger.info("Starting synchronous report generation...");
            ReportService reportService = new ReportService();
            List<String> selectedSportNames = Arrays.stream(SportType.values())
                    .map(SportType::getDisplayName)
                    .toList();
            ReportResult reportResult = reportService.generateReport(selectedSportNames);

            logger.info("Report generation completed. Starting report printing...");
            ReportPrintService printService = new ReportPrintService(reportResult);
            printService.printReport();

            logger.info("Report generation finished.");

        } catch (ApiException e) {
            logger.error("Application error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
