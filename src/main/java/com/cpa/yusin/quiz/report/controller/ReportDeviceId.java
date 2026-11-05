package com.cpa.yusin.quiz.report.controller;

/**
 * 앱이 {@code X-Device-Id} 헤더로 보낸 익명 기기 ID. 소문자로 정규화된 UUID 문자열이다.
 * <p>
 * 컨트롤러 메서드의 첫 인자로 두면 요청 본문 검증보다 먼저 확인된다.
 * </p>
 */
public record ReportDeviceId(String value) {
    public static final String HEADER = "X-Device-Id";
}
