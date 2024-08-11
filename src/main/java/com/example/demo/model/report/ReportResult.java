package com.example.demo.model.report;

import lombok.Data;

import java.util.List;

@Data
public class ReportResult {
    private final List<SportReport> sportReports;

    public ReportResult(List<SportReport> sportReports) {
        this.sportReports = sportReports;
    }
}