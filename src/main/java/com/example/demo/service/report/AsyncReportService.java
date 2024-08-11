package com.example.demo.service.report;

import com.example.demo.model.Sport;
import com.example.demo.model.report.ReportResult;
import com.example.demo.model.report.SportReport;
import com.example.demo.service.SportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Service class for generating reports asynchronously.
 * Uses a thread pool to process multiple sports concurrently.
 */
public class AsyncReportService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncReportService.class);
    private static final int MAX_THREADS = 3;
    private final SportService sportService;
    private final ReportService reportService;
    private final ExecutorService executorService;

    public AsyncReportService() {
        this.sportService = new SportService();
        this.reportService = new ReportService(this.sportService);
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }

    /**
     * Asynchronously generates a report based on the specified sports names.
     * This method fetches sports data, filters it according to the provided list of sport names,
     * and processes each sport asynchronously. The results are then aggregated into a single report.
     *
     * @param selectedSportNames A list of sport names to filter the sports data.
     *                           If null or empty, all sports will be included in the report.
     * @return A {@link CompletableFuture} that, when completed, contains the {@link ReportResult}
     *         generated from the filtered sports data.
     *
     * The {@link CompletableFuture} returned will complete once all sport reports have been processed
     * and aggregated into the final report. If an error occurs during the asynchronous processing,
     * the {@link CompletableFuture} will complete exceptionally.
     */
    public CompletableFuture<ReportResult> generateReportAsync(List<String> selectedSportNames) {
        return CompletableFuture.supplyAsync(() -> {
            List<Sport> sports = sportService.fetchSportsData();

            if (selectedSportNames != null && selectedSportNames.size() > 0) {
                sports = sports.stream()
                        .filter(o -> selectedSportNames.contains(o.getName()))
                        .toList();
            }

            List<CompletableFuture<SportReport>> sportReportFutures = sports.stream()
                    .map(this::processSportAsync)
                    .toList();

            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                    sportReportFutures.toArray(CompletableFuture[]::new)
            );

            return allOf.thenApply(v -> sportReportFutures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList()))
                    .thenApply(ReportResult::new)
                    .join();
        }, executorService);
    }

    private CompletableFuture<SportReport> processSportAsync(Sport sport) {
        return CompletableFuture.supplyAsync(() -> reportService.processSport(sport), executorService);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
