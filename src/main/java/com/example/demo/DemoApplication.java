package com.example.demo;

import com.example.demo.exception.ApiException;
import com.example.demo.model.SportType;
import com.example.demo.service.TopLeagueMarketsReportService;
import com.example.demo.utils.PerformanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
    public static void main(String[] args) {
        //printTopLeaguesMarketReport();

        PerformanceUtils.measureRuntime(
                DemoApplication::printTopLeaguesMarketReport, "printTopLeaguesMarketReport()");
    }

    private static void printTopLeaguesMarketReport() {
        logger.info(String.format("Printing of %s started...", TopLeagueMarketsReportService.NAME));

        try {
            TopLeagueMarketsReportService report = new TopLeagueMarketsReportService();

            for (SportType sportType : SportType.values()) {
                report.printReportToConsole(sportType);
            }
        } catch (ApiException e) {
            logger.error("Application encountered error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info(String.format("Printing of %s finished.", TopLeagueMarketsReportService.NAME));
    }
}
