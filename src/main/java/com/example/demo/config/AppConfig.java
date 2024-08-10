package com.example.demo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_PROPERTIES_PATH = "src/main/resources/application.properties";
    private final Properties properties = new Properties();

    public AppConfig() {
        try (InputStream input = new FileInputStream(CONFIG_PROPERTIES_PATH)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getApplicationName() {
        return properties.getProperty("spring.application.name");
    }

    public String getSportsUrl() {
        return properties.getProperty("api.url.sports");
    }

    public String getEventsUrl(long leagueId) {
        return String.format(properties.getProperty("api.url.events"), leagueId);
    }

    public String getEventDetailsUrl(long eventId) {
        return String.format(properties.getProperty("api.url.event.details"), eventId);
    }
}
