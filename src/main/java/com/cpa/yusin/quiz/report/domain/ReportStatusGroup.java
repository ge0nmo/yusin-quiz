package com.cpa.yusin.quiz.report.domain;

import java.util.List;

/** 관리자 목록의 상태 탭. */
public enum ReportStatusGroup {
    OPEN(ReportStatus.OPEN),
    RESOLVED(List.of(ReportStatus.RESOLVED)),
    REJECTED(List.of(ReportStatus.REJECTED)),
    ALL(List.of(ReportStatus.values()));

    private final List<ReportStatus> statuses;

    ReportStatusGroup(List<ReportStatus> statuses) {
        this.statuses = statuses;
    }

    public List<ReportStatus> statuses() {
        return statuses;
    }
}
