package com.cpa.yusin.quiz.global.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException
{
    private final ExceptionMessage exceptionMessage;

    public GlobalException(ExceptionMessage exceptionMessage)
    {
        super(exceptionMessage.getMessage());
        this.exceptionMessage = exceptionMessage;
    }
}
