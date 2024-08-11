package com.example.demo.model.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeagueReport {
    private final String leagueName;
    private final List<EventReport> eventReports = new ArrayList<>();

    public LeagueReport(String leagueName) {
        this.leagueName = leagueName;
    }

    public void addEventReport(EventReport eventReport) {
        eventReports.add(eventReport);
    }
}
