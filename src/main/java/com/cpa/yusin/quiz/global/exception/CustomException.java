package com.cpa.yusin.quiz.global.exception;

import lombok.Getter;

@Getter
public abstract class CustomException extends RuntimeException
{
    private final ExceptionMessage exceptionMessage;

    public CustomException(ExceptionMessage exceptionMessage)
    {
        super(exceptionMessage.getMessage());
        this.exceptionMessage = exceptionMessage;
    }
}
