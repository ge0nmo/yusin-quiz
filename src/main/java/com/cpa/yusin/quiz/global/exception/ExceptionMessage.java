package com.cpa.yusin.quiz.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionMessage {
    INVALID_LOGIN_INFORMATION("로그인 정보가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),

    EMAIL_EXISTS("사용중인 이메일입니다.", HttpStatus.CONFLICT),

    NO_AUTHORIZATION("권한이 없습니다", HttpStatus.FORBIDDEN),

    USER_NOT_FOUND("회원 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    INVALID_EMAIL("이메일 정보가 유효하지 않습니다", HttpStatus.BAD_REQUEST),

    SUBJECT_NOT_FOUND("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    SUBJECT_NAME_EXIST("해당 과목 이름이 존재합니다.", HttpStatus.CONFLICT),

    EXAM_NOT_FOUND("해당 시험 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    EXAM_DUPLICATED("동일한 시험 정보가 존재합니다", HttpStatus.CONFLICT),

    PROBLEM_NOT_FOUND("해당 문제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PROBLEM_NUMBER_EXISTS("해당 문제 번호가 이미 존재합니다.", HttpStatus.CONFLICT),

    CHOICE_NOT_FOUND("해당 보기 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    INVALID_DATA("유효하지 않은 데이터입니다.", HttpStatus.BAD_REQUEST),

    ANSWER_NOT_FOUND("해당 답변 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    /**
     * question
     */

    QUESTION_NOT_FOUND("해당 질문 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    INVALID_QUESTION_PASSWORD("비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED),

    QUESTION_HAS_ANSWERS("답글이 달린 질문은 삭제할 수 없습니다", HttpStatus.BAD_REQUEST),

    /**
     * answer
     */
    INVALID_ANSWER_PASSWORD("비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED),

    /**
     * subscription plan
     */

    PLAN_NOT_FOUND("해당 상품을 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    PLAN_DUPLICATED("동일한 상품 정보가 존재합니다.", HttpStatus.CONFLICT),

    /**
     * payment
     */
    PAYMENT_NOT_FOUND("결제 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

    PAYMENT_NOT_COMPLETED("결제에 실패했습니다", HttpStatus.BAD_REQUEST),

    PAYMENT_PRICE_ERROR("결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // Study Session
    SESSION_NOT_FOUND("세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // Server
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * subscription
     */
    SUBSCRIPTION_NOT_FOUND("구독 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SUBSCRIPTION_EXISTS("이미 구독 중입니다.", HttpStatus.CONFLICT),

    /**
     * bookmark
     */
    BOOKMARK_NOT_FOUND("해당 북마크 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOOKMARK_ALREADY_EXISTS("이미 북마크된 문제입니다.", HttpStatus.CONFLICT),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ExceptionMessage(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
