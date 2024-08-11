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

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for processing sports data and generating detailed reports.
 * Handles the creation of ReportResult, SportReport, LeagueReport, and EventReport objects.
 */
public class ReportService {

    private static final int DEFAULT_MATCHES_LIMIT = 2;
    private final SportService sportService;

    public ReportService() {
        this.sportService = new SportService();
    }

    public ReportService(SportService sportService) {
        this.sportService = sportService;
    }

    public SportReport processSport(Sport sport) {
        SportReport sportReport = new SportReport(sport.getName());
        List<League> topLeagues = sportService.getTopLeagues(sport);

        for (League league : topLeagues) {
            LeagueReport leagueReport = processLeague(league);
            sportReport.addLeagueReport(leagueReport);
        }

        return sportReport;
    }

    public LeagueReport processLeague(League league) {
        LeagueReport leagueReport = new LeagueReport(league.getName());
        List<Event> topMatches = sportService.fetchTopMatches(league.getId(), DEFAULT_MATCHES_LIMIT);

        for (Event event : topMatches) {
            EventReport eventReport = processEvent(event);
            leagueReport.addEventReport(eventReport);
        }

        return leagueReport;
    }

    public EventReport processEvent(Event event) {
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

    /**
     * Synchronously generates a report based on the specified sports names.
     * This method fetches sports data, filters it according to the provided list of sport names,
     * processes each sport sequentially, and aggregates the results into a single report.
     *
     * @param selectedSportNames A list of sport names to filter the sports data.
     *                           If null or empty, all sports will be included in the report.
     * @return A {@link ReportResult} containing the processed sport reports based on the filtered sports data.
     *
     * This method processes each sport sequentially in the current thread and does not involve any asynchronous operations.
     * It returns the final {@link ReportResult} once all sports have been processed.
     */
    public ReportResult generateReport(List<String> selectedSportNames){
        List<SportReport> sportReports = new ArrayList<>();
        List<Sport> sports = sportService.fetchSportsData();
        if (selectedSportNames != null && selectedSportNames.size() > 0) {
            sports = sports.stream()
                    .filter(o -> selectedSportNames.contains(o.getName()))
                    .toList();
        }

        for (Sport sport : sports) {
            sportReports.add(processSport(sport));
        }
        return new ReportResult(sportReports);
    }
}
