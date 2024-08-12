package com.example.demo.service.report;

import com.example.demo.model.Event;
import com.example.demo.model.EventDetailsResponse;
import com.example.demo.model.League;
import com.example.demo.model.Market;
import com.example.demo.model.Runner;
import com.example.demo.model.Sport;
import com.example.demo.model.report.EventReport;
import com.example.demo.model.report.LeagueReport;
import com.example.demo.model.report.MarketReport;
import com.example.demo.model.report.ReportResult;
import com.example.demo.model.report.RunnerReport;
import com.example.demo.model.report.SportReport;
import com.example.demo.service.AsyncSportService;
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
    private static final int DEFAULT_MATCHES_LIMIT = 2;
    private static final int MAX_THREADS = 3;
    private final AsyncSportService asyncSportService;
    private final ExecutorService executorService;

    public AsyncReportService() {
        this.asyncSportService = new AsyncSportService();
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
            List<Sport> sports = asyncSportService.fetchSportsDataAsync().join();

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
        return CompletableFuture.supplyAsync(() -> processSport(sport), executorService);
    }

    public SportReport processSport(Sport sport) {
        SportReport sportReport = new SportReport(sport.getName());
        List<League> topLeagues = asyncSportService.getTopLeagues(sport);

        for (League league : topLeagues) {
            LeagueReport leagueReport = processLeague(league);
            sportReport.addLeagueReport(leagueReport);
        }

        return sportReport;
    }

    public LeagueReport processLeague(League league) {
        LeagueReport leagueReport = new LeagueReport(league.getName());
        List<Event> topMatches = asyncSportService.fetchTopMatches(league.getId(), DEFAULT_MATCHES_LIMIT);

        for (Event event : topMatches) {
            EventReport eventReport = processEvent(event);
            leagueReport.addEventReport(eventReport);
        }

        return leagueReport;
    }

    public EventReport processEvent(Event event) {
        EventReport eventReport = new EventReport(event.getName(), event.getKickoffUtc() + " UTC", event.getId());
        EventDetailsResponse eventDetails = asyncSportService.fetchEventDetailsAsync(event.getId()).join();

        for (Market market : eventDetails.getMarkets()) {
            MarketReport marketReport = new MarketReport(market.getName());
            for (Runner runner : market.getRunners()) {
                marketReport.addRunner(new RunnerReport(runner.getName(), runner.getPrice(), runner.getId()));
            }
            eventReport.addMarketReport(marketReport);
        }

        return eventReport;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
