package com.example.demo;

import com.example.demo.model.Sport;
import com.example.demo.model.SportType;
import com.example.demo.model.report.ReportResult;
import com.example.demo.model.report.SportReport;
import com.example.demo.service.AsyncSportService;
import com.example.demo.utils.PerformanceUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ParallelismTest {

    private static final Logger logger = LoggerFactory.getLogger(ParallelismTest.class);
    private static final List<String> selectedSportNames =  Arrays.stream(SportType.values())
            .map(SportType::getDisplayName)
            .toList();

    @Test
    void testGenerateReportTime() {
        PerformanceUtils.measureRuntime(this::testGenerateReport, "Asynchronous report generation");
    }

    @Test
    public void testGenerateReport(){
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        AsyncSportService asyncSportService = new AsyncSportService();
        try {
            ReportResult reportResult = generateReportAsync(selectedSportNames, executorService, asyncSportService).join();
            logger.info("Report result, n sports: " + reportResult.getSportReports().size());
        } finally {
            executorService.shutdown();
        }
    }

    public CompletableFuture<ReportResult> generateReportAsync(List<String> selectedSportNames,
                                                               ExecutorService executorService,
                                                               AsyncSportService asyncSportService) {
        return CompletableFuture.supplyAsync(() -> {
            return composeReportAsync(selectedSportNames, asyncSportService).join();
        }, executorService);
    }

    public CompletableFuture<ReportResult> composeReportAsync(List<String> selectedSportNames,
                                                               AsyncSportService asyncSportService) {

        return asyncSportService.fetchSportsDataAsync()
                .thenApply(sports -> {
                    List<Sport> filteredSports = sports;
                    if (selectedSportNames != null && selectedSportNames.size() > 0) {
                        filteredSports = sports.stream()
                                .filter(o -> selectedSportNames.contains(o.getName()))
                                .toList();
                    }

                    List<CompletableFuture<SportReport>> sportReportFutures = filteredSports.stream()
                            .map(this::processSportAsyncPlug)
                            .toList();

                    CompletableFuture<Void> allOf = CompletableFuture.allOf(
                            sportReportFutures.toArray(CompletableFuture[]::new)
                    );

                    return allOf.thenApply(v -> sportReportFutures.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList()))
                            .thenApply(sportReports -> new ReportResult(sportReports))
                            .join();
                });
    }


    public CompletableFuture<SportReport> processSportAsyncPlug(Sport sport) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("processSportAsync");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Handle the exception if needed
                Thread.currentThread().interrupt();
            }
            // Return a new, empty SportReport
            return new SportReport(sport.getName());
        });
    }

}
