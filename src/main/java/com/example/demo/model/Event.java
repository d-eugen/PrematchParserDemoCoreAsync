package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    private long id;
    private String name;
    private List<Competitor> competitors;
    private long kickoff;
    private long lastUpdated;
    private League league;
    private String betline;
    private boolean open;
    private String status;
    private String matchPhase;
    private List<Market> markets;

    public LocalDateTime getKickoffUtc() {
        // Convert to Instant
        Instant instant = Instant.ofEpochMilli(kickoff);
        // Convert to LocalDateTime in UTC
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
