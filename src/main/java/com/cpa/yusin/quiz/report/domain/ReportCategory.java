package com.cpa.yusin.quiz.report.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 신고 유형. 문제 오류 6종은 문제가 반드시 있어야 하고, 건의·문의 2종은 문제 없이도 보낼 수 있다.
 * <p>
 * 내용 필수 유형은 공백을 뺀 내용이 {@link #MIN_REQUIRED_CONTENT_LENGTH}자 이상이어야 한다.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public enum ReportCategory {
    WRONG_ANSWER(ReportCategoryGroup.PROBLEM_ERROR, true),
    EXPLANATION(ReportCategoryGroup.PROBLEM_ERROR, false),
    TYPO(ReportCategoryGroup.PROBLEM_ERROR, false),
    BROKEN_MEDIA(ReportCategoryGroup.PROBLEM_ERROR, false),
    OUTDATED(ReportCategoryGroup.PROBLEM_ERROR, false),
    OTHER_ERROR(ReportCategoryGroup.PROBLEM_ERROR, true),
    SUGGESTION(ReportCategoryGroup.FEEDBACK, true),
    INQUIRY(ReportCategoryGroup.FEEDBACK, true);

    public static final int MIN_REQUIRED_CONTENT_LENGTH = 5;

    private final ReportCategoryGroup group;
    private final boolean contentRequired;

    public boolean isProblemRequired() {
        return group == ReportCategoryGroup.PROBLEM_ERROR;
    }

    /** 묶음이 없으면 모든 유형이다. */
    public static List<ReportCategory> of(ReportCategoryGroup group) {
        return Arrays.stream(values()).filter(category -> group == null || category.group == group).toList();
    }
}
