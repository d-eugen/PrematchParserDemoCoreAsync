package com.example.demo.model.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MarketReport {
    private final String marketName;
    private final List<RunnerReport> runnerReports = new ArrayList<>();

    public MarketReport(String marketName) {
        this.marketName = marketName;
    }

    public void addRunner(RunnerReport runnerReport) {
        runnerReports.add(runnerReport);
    }
}
