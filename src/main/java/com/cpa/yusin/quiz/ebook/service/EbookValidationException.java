package com.cpa.yusin.quiz.ebook.service;

/** 관리자가 원문 또는 수록 범위를 수정할 수 있도록 맥락이 있는 메시지를 전달합니다. */
public class EbookValidationException extends RuntimeException {
    public EbookValidationException(String message) { super(message); }
}
