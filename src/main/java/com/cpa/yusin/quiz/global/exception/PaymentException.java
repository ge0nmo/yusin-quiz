package com.cpa.yusin.quiz.global.exception;

public class PaymentException extends CustomException
{
    public PaymentException(ExceptionMessage exceptionMessage)
    {
        super(exceptionMessage);
    }
}
