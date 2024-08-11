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
