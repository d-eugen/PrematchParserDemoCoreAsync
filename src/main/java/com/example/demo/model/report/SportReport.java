package com.example.demo.model.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SportReport {
    private final String sportName;
    private final List<LeagueReport> leagueReports = new ArrayList<>();

    public SportReport(String sportName) {
        this.sportName = sportName;
    }

    public void addLeagueReport(LeagueReport leagueReport) {
        leagueReports.add(leagueReport);
    }
}
