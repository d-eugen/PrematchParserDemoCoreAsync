# Sports Data API Integration

This project demonstrates the integration with a sports data API, providing functionality to fetch and process sports-related information such as leagues, events, and betting markets.

## Key Components
1. **ApiService**: Handles HTTP requests to the external API.
2. **SportService**: Manages sports-related data fetching and processing.
3. **AsyncReportService**: Generates reports asynchronously.
4. **ReportPrintService**: Formats and prints generated reports.
5. **ReportService**: Processes sports data and generates detailed reports.

## Features
- Fetch sports data, including leagues and events
- Generate reports for top leagues and matches
- Asynchronous report generation for improved performance
- Customizable report output

## Usage
For asynchronous report generation:
```
AsyncReportService asyncReportService = new AsyncReportService();
CompletableFuture<ReportResult> futureResult = asyncReportService.generateReportAsync(List.of("Football", "Basketball"));

// Do other work while report is being generated

ReportResult result = futureResult.join();
ReportPrintService printService = new ReportPrintService(result);
printService.printReport();

asyncReportService.shutdown();
```
To generate a report synchronously:
```
ReportService reportService = new ReportService();
ReportResult result = reportService.generateReport(List.of("Football", "Basketball"));

ReportPrintService printService = new ReportPrintService(result);
printService.printReport();
```

## Dependencies
- Java 11+
- Jackson for JSON processing
- SLF4J for logging

## Note
This project is a demonstration and may require additional error handling and testing for production use.

## Running the Application
To run this application, use the following command:
```
mvn exec:java
```