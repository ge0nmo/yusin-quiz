package com.cpa.yusin.quiz.global.exception;

import lombok.Getter;

@Getter
public class StudySessionException extends RuntimeException {
    private final ExceptionMessage exceptionMessage;

    public StudySessionException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage());
        this.exceptionMessage = exceptionMessage;
    }
}
