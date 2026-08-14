package com.cpa.yusin.quiz.version.domain;

import java.util.Locale;
import java.util.Optional;

public enum AppPlatform {
    ANDROID,
    IOS;

    public static Optional<AppPlatform> from(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
