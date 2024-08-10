package com.example.demo.model;

public enum SportType {
    FOOTBALL("Football"),
    TENNIS("Tennis"),
    HOCKEY("Hockey"),
    BASKETBALL("Basketball");

    private final String displayName;

    SportType (String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
