package com.example.demo.model.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventReport {
    private final String eventName;
    private final String kickoffUtc;
    private final long eventId;
    private final List<MarketReport> marketReports = new ArrayList<>();

    public EventReport(String eventName, String kickoffUtc, long eventId) {
        this.eventName = eventName;
        this.kickoffUtc = kickoffUtc;
        this.eventId = eventId;
    }

    public void addMarketReport(MarketReport marketReport) {
        marketReports.add(marketReport);
    }
}
