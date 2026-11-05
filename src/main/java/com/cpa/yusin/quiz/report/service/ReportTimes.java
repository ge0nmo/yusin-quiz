package com.cpa.yusin.quiz.report.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 신고 응답의 시각은 초 단위로 자른다. 형식은 기존 {@code LocalDateTime} 직렬화(ISO-8601, 타임존 없음, 서버 시간)
 * 그대로이고 소수 초만 없앤다. 앱이 "새 답변"을 answeredAt 문자열로 비교하므로 같은 값은 항상 같은 문자열이어야 한다.
 */
final class ReportTimes {
    private ReportTimes() {}

    static LocalDateTime of(LocalDateTime value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.SECONDS);
    }
}
