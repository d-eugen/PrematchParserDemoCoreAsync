package com.example.demo.service.report;

import com.example.demo.model.report.EventReport;
import com.example.demo.model.report.LeagueReport;
import com.example.demo.model.report.MarketReport;
import com.example.demo.model.report.ReportResult;
import com.example.demo.model.report.RunnerReport;
import com.example.demo.model.report.SportReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service class for formatting and printing generated reports.
 * Provides a hierarchical output of sports, leagues, events, and markets.
 */
public class ReportPrintService {

    private static final Logger logger = LoggerFactory.getLogger(ReportPrintService.class);
    private static final String INDENT = "   ";
    public final ReportResult reportResult;

    public ReportPrintService(ReportResult reportResult) {
        this.reportResult = reportResult;
    }

    /**
     * Prints the entire report to the console.
     */
    public void printReport() {
        List<SportReport> sportReports = reportResult.getSportReports();
        for (SportReport sportReport : sportReports) {
            for (LeagueReport leagueReport : sportReport.getLeagueReports()) {
                printIndented(1, sportReport.getSportName(), leagueReport.getLeagueName());
                for (EventReport eventReport : leagueReport.getEventReports()) {
                    printIndented(2, eventReport.getEventName(),
                            (eventReport.getKickoffUtc() + " UTC"), eventReport.getEventId());
                    for (MarketReport marketReport : eventReport.getMarketReports()) {
                        printIndented(3, marketReport.getMarketName());
                        for (RunnerReport runnerReport : marketReport.getRunnerReports()) {
                            printIndented(4, runnerReport.getRunnerName(), runnerReport.getPrice(),
                                    runnerReport.getRunnerId());
                        }
                    }
                }
            }
        }
    }

    private String getIndent(int n) {
        return INDENT.repeat(n);
    }

    private void printIndented(int indentLevel, Object... values) {
        String indent = getIndent(indentLevel);
        StringBuilder formatBuilder = new StringBuilder(indent);
        for (int i = 0; i < values.length; i++) {
            formatBuilder.append("%s");
            if (i < values.length - 1) {
                formatBuilder.append(", ");
            }
        }
        String message = String.format(formatBuilder.toString(), values);

        //logger.info(message);
        System.out.println(message); // More concise way to print output without logging info
    }
}
