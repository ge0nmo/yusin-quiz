package com.cpa.yusin.quiz.question.service.dto;

import org.springframework.util.StringUtils;

public record AdminQuestionSearchCondition(AdminQuestionStatus status,
                                           String keyword,
                                           AdminQuestionDatePreset datePreset) {

    public static AdminQuestionSearchCondition all() {
        return of(AdminQuestionStatus.ALL, null, AdminQuestionDatePreset.ALL);
    }

    public static AdminQuestionSearchCondition of(AdminQuestionStatus status, String keyword) {
        return of(status, keyword, AdminQuestionDatePreset.ALL);
    }

    public static AdminQuestionSearchCondition of(AdminQuestionStatus status,
                                                  String keyword,
                                                  AdminQuestionDatePreset datePreset) {
        AdminQuestionStatus normalizedStatus = status == null ? AdminQuestionStatus.ALL : status;
        AdminQuestionDatePreset normalizedDatePreset = datePreset == null ? AdminQuestionDatePreset.ALL : datePreset;

        // Blank keywords behave like an omitted filter so the repository can skip the LIKE clauses entirely.
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        return new AdminQuestionSearchCondition(normalizedStatus, normalizedKeyword, normalizedDatePreset);
    }

    public Boolean answeredByAdminFilter() {
        return switch (status) {
            case ALL -> null;
            case ANSWERED -> true;
            case UNANSWERED -> false;
        };
    }

    public boolean requiresTodayBoundary() {
        return datePreset == AdminQuestionDatePreset.TODAY;
    }
}
