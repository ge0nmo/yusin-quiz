package com.cpa.yusin.quiz.global.exception;

public class ProblemException extends CustomException
{
    public ProblemException(ExceptionMessage exceptionMessage)
    {
        super(exceptionMessage);
    }
}
