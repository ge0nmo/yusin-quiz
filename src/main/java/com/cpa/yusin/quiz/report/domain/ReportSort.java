package com.cpa.yusin.quiz.report.domain;

/** 관리자 목록 정렬. SAME_PROBLEM은 같은 문제의 미처리 신고 수가 많은 순, 같으면 최신순이다. */
public enum ReportSort {
    SAME_PROBLEM,
    LATEST,
    OLDEST
}
