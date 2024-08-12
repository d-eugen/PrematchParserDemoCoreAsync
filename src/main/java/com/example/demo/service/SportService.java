package com.example.demo.service;
import com.example.demo.config.AppConfig;
import com.example.demo.model.Event;
import com.example.demo.model.EventDetailsResponse;
import com.example.demo.model.EventResponse;
import com.example.demo.model.League;
import com.example.demo.model.Market;
import com.example.demo.model.Sport;
import com.example.demo.model.SportType;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service class for fetching and processing sports-related data.
 * Provides methods to retrieve information about sports, leagues, and events.
 */
public class SportService {

    private final AsyncApiService asyncApiService;
    private final AppConfig appConfig;

    public SportService() {
        this.asyncApiService = new AsyncApiService();
        this.appConfig = new AppConfig();
    }

    public List<Sport> fetchSportsData() {
        return fetchSportsDataAsync().join();
    }

    public EventResponse fetchEventsData(long leagueId) {
        return fetchEventsDataAsync(leagueId).join();
    }

    public EventDetailsResponse fetchEventDetails(long eventId) {
        return fetchEventDetailsAsync(eventId).join();
    }

    public CompletableFuture<List<Sport>> fetchSportsDataAsync() {
        return asyncApiService.fetchDataAsync(appConfig.getSportsUrl(), new TypeReference<List<Sport>>() {});
    }

    public CompletableFuture<EventResponse> fetchEventsDataAsync(long leagueId) {
        return asyncApiService.fetchDataAsync(appConfig.getEventsUrl(leagueId), EventResponse.class);
    }

    public CompletableFuture<EventDetailsResponse> fetchEventDetailsAsync(long eventId) {
        return asyncApiService.fetchDataAsync(appConfig.getEventDetailsUrl(eventId), EventDetailsResponse.class);
    }

    public List<League> getTopLeagues(SportType sportType) {
        List<Sport> allSports = fetchSportsData();
        return allSports.stream()
                .filter(sport -> sport.getName().equalsIgnoreCase(sportType.getDisplayName()))
                .flatMap(sport -> sport.getRegions().stream())
                .flatMap(region -> region.getLeagues().stream())
                .filter(League::isTop)
                .sorted(Comparator.comparing(League::getTopOrder))
                .collect(Collectors.toList());
    }

    public List<League> getTopLeagues(Sport sport) {
        return sport.getRegions().stream()
                .flatMap(region -> region.getLeagues().stream())
                .filter(League::isTop)
                .sorted(Comparator.comparing(League::getTopOrder))
                .collect(Collectors.toList());
    }

    public List<Event> fetchAllEvents(long leagueId) {
        EventResponse eventResponse = fetchEventsData(leagueId);
        return eventResponse.getData();
    }

    public List<Event> fetchTopMatches(long leagueId, int limit) {
        return fetchAllEvents(leagueId).stream()
                .filter(event -> "prematch".equalsIgnoreCase(event.getBetline()))
                .sorted(Comparator.comparingLong(Event::getKickoff))    // Sort by kickoff to get the closest event on top
                                                                        // (assuming all events are in future)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Market findMarketByName(EventDetailsResponse eventDetailsResponse, String marketName) {
        return eventDetailsResponse.getMarkets().stream()
                .filter(market -> marketName.equalsIgnoreCase(market.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Market not found: " + marketName));
    }
}