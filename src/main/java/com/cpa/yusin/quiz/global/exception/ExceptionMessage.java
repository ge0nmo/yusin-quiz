package com.cpa.yusin.quiz.global.exception;

import lombok.Getter;

@Getter
public enum ExceptionMessage
{
    INVALID_LOGIN_INFORMATION("로그인 정보가 일치하지 않습니다."),

    USER_NOT_FOUND("회원 정보를 찾을 수 없습니다"),

    INVALID_EMAIL("이메일 정보가 유효하지 않습니다"),

    SUBJECT_NOT_FOUND("해당 과목을 찾을 수 없습니다."),

    SUBJECT_NAME_EXISTS("해당 과목 이름이 존재합니다.")
;

    private final String message;

    ExceptionMessage(String message)
    {
        this.message = message;
    }
}
