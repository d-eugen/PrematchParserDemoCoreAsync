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
    private static final int DEFAULT_MATCHES_LIMIT = 2;

    private final SportService sportService;
    private final ExecutorService executorService;

    public AsyncReportService() {
        this.sportService = new SportService();
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }

    public CompletableFuture<ReportResult> generateReport(List<String> selectedSportNames) {
        return CompletableFuture.supplyAsync(() -> {
            List<Sport> sports = sportService.fetchSportsData();

            if (selectedSportNames != null && selectedSportNames.size() != 0) {
                sports = sports.stream()
                        .filter(o -> selectedSportNames.contains(o.getName()))
                        .toList();
            }

            List<CompletableFuture<SportReport>> sportReportFutures = sports.stream()
                    .map(this::processSport)
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

    private CompletableFuture<SportReport> processSport(Sport sport) {
        return CompletableFuture.supplyAsync(() -> {
            SportReport sportReport = new SportReport(sport.getName());
            List<League> topLeagues = sportService.getTopLeagues(sport);

            for (League league : topLeagues) {
                LeagueReport leagueReport = processLeague(league);
                sportReport.addLeagueReport(leagueReport);
            }

            return sportReport;
        }, executorService);
    }

    private LeagueReport processLeague(League league) {
        LeagueReport leagueReport = new LeagueReport(league.getName());
        List<Event> topMatches = sportService.fetchTopMatches(league.getId(), DEFAULT_MATCHES_LIMIT);

        for (Event event : topMatches) {
            EventReport eventReport = processEvent(event);
            leagueReport.addEventReport(eventReport);
        }

        return leagueReport;
    }

    private EventReport processEvent(Event event) {
        EventReport eventReport = new EventReport(event.getName(), event.getKickoffUtc() + " UTC", event.getId());
        EventDetailsResponse eventDetails = sportService.fetchEventDetails(event.getId());

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
