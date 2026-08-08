package com.cpa.yusin.quiz.wordpractice.controller.dto.response;

/**
 * 말문제 subject 진입 화면에서 현재 회차의 상태를 표현한다.
 * 도메인 회차가 아직 없는 경우도 클라이언트가 명확히 구분하도록 NOT_STARTED를 둔다.
 */
public enum WordPracticeProgressStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}
