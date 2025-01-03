package org.example.boredless_backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ActivityType {
    HIKE("Hike"),
    WINE_TASTING("Wine Tasting"),
    BEACH("Beach"),
    DRINKING("Drinking"),
    PARTYING("Partying"),
    SHOPPING("Shopping"),
    CAMPING("Camping"),
    DINING("Dining"),
    BREAKFAST("Breakfast"),
    CAFE("Cafe"),
    SIGHTSEEING("Sightseeing"),
    SURFING("Surfing"),
    BOAT_TOUR("Boat Tour"),
    TOUR("Tour"),
    ARCHITECTURE("Architecture"),
    SNORKELING("Snorkeling"),
    MUSEUM_VISIT("Museum Visit"),
    NATURE_WALK("Nature Walk"),
    PHOTOGRAPHY("Photography"),
    ADVENTURE_SPORTS("Adventure Sports");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ActivityType fromDisplayName(String displayName) {
        for (ActivityType type : ActivityType.values()) {
            if (type.getDisplayName().equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ActivityType: " + displayName);
    }
}


