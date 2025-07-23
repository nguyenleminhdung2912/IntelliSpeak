package com.gsu25se05.itellispeak.entity;

public enum TipType {
    GOOD,
    IMPROVE,
    WARNING,
    DANGEROUS,
    REDUNDANT,
    MISSING,
    INCONSISTENT,
    NEUTRAL,        // New
    POSITIVE,       // Optional
    NEGATIVE,       // Optional
    INFO,           // Optional
    NOTE;           // Optional

    public static TipType fromString(String value) {
        return switch (value.toLowerCase()) {
            case "good" -> GOOD;
            case "improve" -> IMPROVE;
            case "warning" -> WARNING;
            case "dangerous" -> DANGEROUS;
            case "redundant" -> REDUNDANT;
            case "missing" -> MISSING;
            case "inconsistent" -> INCONSISTENT;
            case "neutral" -> NEUTRAL;
            case "positive" -> POSITIVE;
            case "negative" -> NEGATIVE;
            case "info" -> INFO;
            case "note" -> NOTE;
            default -> null;
        };
    }
}

