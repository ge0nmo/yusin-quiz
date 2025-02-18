package com.cpa.yusin.quiz.global.exception;

import lombok.Getter;

@Getter
public enum ExceptionMessage
{
    INVALID_LOGIN_INFORMATION("로그인 정보가 일치하지 않습니다."),

    EMAIL_EXISTS("사용중인 이메일입니다."),

    NO_AUTHORIZATION("권한이 없습니다"),

    USER_NOT_FOUND("회원 정보를 찾을 수 없습니다"),

    INVALID_EMAIL("이메일 정보가 유효하지 않습니다"),

    SUBJECT_NOT_FOUND("해당 과목을 찾을 수 없습니다."),

    SUBJECT_NAME_EXIST("해당 과목 이름이 존재합니다."),

    EXAM_NOT_FOUND("해당 시험 정보를 찾을 수 없습니다."),

    EXAM_DUPLICATED("동일한 시험 정보가 존재합니다"),

    PROBLEM_NOT_FOUND("해당 문제 정보를 찾을 수 없습니다."),

    CHOICE_NOT_FOUND("해당 보기 정보를 찾을 수 없습니다."),

    INVALID_DATA("유효하지 않은 데이터입니다."),


    ANSWER_NOT_FOUND("해당 답변 정보를 찾을 수 없습니다."),

    /**
     * question
     */

    QUESTION_NOT_FOUND("해당 질문 정보를 찾을 수 없습니다."),

    INVALID_QUESTION_PASSWORD("비밀번호가 일치하지 않습니다"),

    QUESTION_HAS_ANSWERS("답글이 달린 질문은 삭제할 수 없습니다"),

    /**
     *  answer
     */
    INVALID_ANSWER_PASSWORD("비밀번호가 일치하지 않습니다"),

    /**
     * subscription plan
     */

    PLAN_NOT_FOUND("해당 상품을 찾을 수 없습니다"),

    PLAN_DUPLICATED("동일한 상품 정보가 존재합니다."),

    /**
     * payment
     */
    PAYMENT_NOT_FOUND("결제 정보를 찾을 수 없습니다"),

    PAYMENT_NOT_COMPLETED("결제에 실패했습니다"),

    PAYMENT_PRICE_ERROR("결제 금액이 일치하지 않습니다."),

    /**
     * subscription
     */
    SUBSCRIPTION_NOT_FOUND("구독 데이터를 찾을 수 없습니다."),
    SUBSCRIPTION_EXISTS("이미 구독 중입니다."),
    ;

    private final String message;

    ExceptionMessage(String message)
    {
        this.message = message;
    }
}
