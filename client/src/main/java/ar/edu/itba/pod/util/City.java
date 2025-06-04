package ar.edu.itba.pod.util;

public enum City {
    NYC,
    CHI;

    public static City fromString(String value) {
        return switch (value.toLowerCase()) {
            case "nyc", "new york", "new york city" -> NYC;
            case "chi", "chicago" -> CHI;
            default -> throw new IllegalArgumentException("Unknown city: " + value);
        };
    }
}

