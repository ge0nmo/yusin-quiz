package com.cpa.yusin.quiz.global.exception;

import lombok.Getter;

public class GlobalException extends RuntimeException
{
    @Getter
    private ExceptionMessage exceptionMessage;

    public GlobalException(ExceptionMessage exceptionMessage)
    {
        super(exceptionMessage.getMessage());
        this.exceptionMessage = exceptionMessage;
    }
}
