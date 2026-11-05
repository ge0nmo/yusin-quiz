package com.cpa.yusin.quiz.report.domain;

import java.util.List;

/** 처리 상태. RECEIVED·IN_REVIEW는 미처리(open), RESOLVED·REJECTED는 종료(closed)다. */
public enum ReportStatus {
    RECEIVED,
    IN_REVIEW,
    RESOLVED,
    REJECTED;

    public static final List<ReportStatus> OPEN = List.of(RECEIVED, IN_REVIEW);

    public boolean isOpen() {
        return OPEN.contains(this);
    }

    public boolean isClosed() {
        return !isOpen();
    }
}
