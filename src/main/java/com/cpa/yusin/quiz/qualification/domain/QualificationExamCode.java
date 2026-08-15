package com.cpa.yusin.quiz.qualification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum QualificationExamCode {
    APPRAISER("감정평가사"),
    CPA("회계사"),
    CUSTOMS_BROKER("관세사"),
    REAL_ESTATE_AGENT("공인중개사");

    private final String displayName;

    public static Optional<QualificationExamCode> from(String value) {
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
