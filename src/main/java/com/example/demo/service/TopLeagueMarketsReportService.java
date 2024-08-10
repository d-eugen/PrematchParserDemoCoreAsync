package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.EventDetailsResponse;
import com.example.demo.model.League;
import com.example.demo.model.Market;
import com.example.demo.model.Runner;
import com.example.demo.model.SportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The {@code TopLeagueMarketsReportService} class is responsible for generating and printing reports related to
 * top league markets.
 */
public class TopLeagueMarketsReportService {
    private static final Logger logger = LoggerFactory.getLogger(TopLeagueMarketsReportService.class);
    private static final String INDENT = "   ";
    public static final String NAME = "Top League's Market Report";
    public static final int DEFAULT_MATCHES_LIMIT = 2;

    private final SportService sportService;

    public TopLeagueMarketsReportService() {
        this.sportService =new SportService();
    }

    /**
     * Generates and prints a report to the console for the top leagues, matches, markets, and runners
     * based on the provided sport type. The report is formatted with indentation to clearly display the
     * hierarchical relationship between leagues, matches, markets, and runners.
     *
     * <p>The report includes:</p>
     * <ul>
     *   <li>The top leagues for the specified {@link SportType}.</li>
     *   <li>The top matches within each league.</li>
     *   <li>The markets for each match.</li>
     *   <li>The runners within each market.</li>
     * </ul>
     *
     * <p>
     * The method retrieves data through the {@link SportService} and prints it in a structured, indented
     * format, making it easy to read in the console.
     * </p>
     *
     * <p><b>Example Output:</b></p>
     * <pre>
     *   Football, Premier League
     *       Manchester United vs Chelsea, 2024-08-10 16:00 UTC, 12345
     *           Match Odds
     *               Manchester United, 1.50, 12231232
     *               Chelsea, 2.60, 28732874
     *           Over/Under 2.5 Goals
     *               Over 2.5 Goals, 1.90, 11282733
     *               Under 2.5 Goals, 1.95, 11827764
     * </pre>
     *
     * @param sportType The {@link SportType} for which the report should be generated.
     *                  This parameter determines the leagues and events that will be included in the report.
     */
    public void printReportToConsole(SportType sportType) {
        List<League> topLeagues = sportService.getTopLeagues(sportType);
        for (League league : topLeagues) {
            printIndented(1, sportType.getDisplayName(), league.getName());
            List<Event> topMatches = sportService.fetchTopMatches(league.getId(), DEFAULT_MATCHES_LIMIT);

            for (Event event : topMatches) {
                printIndented(2, event.getName(), (event.getKickoffUtc() + " UTC"), event.getId());
                EventDetailsResponse eventDetails = sportService.fetchEventDetails(event.getId());
                List<Market> markets = eventDetails.getMarkets();

                for (Market market : markets) {
                    printIndented(3, market.getName());

                    for (Runner runner : market.getRunners()) {
                        printIndented(4, runner.getName(), runner.getPrice(), runner.getId());
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
