package com.example.demo.model.report;

import lombok.Data;

@Data
public class RunnerReport {
    private final String runnerName;
    private final double price;
    private final long runnerId;

    public RunnerReport(String runnerName, double price, long runnerId) {
        this.runnerName = runnerName;
        this.price = price;
        this.runnerId = runnerId;
    }
}
