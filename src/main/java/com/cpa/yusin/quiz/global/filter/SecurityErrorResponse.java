package com.cpa.yusin.quiz.global.filter;

public record SecurityErrorResponse(
        int status,
        String code,
        String message,
        String path
) {
    public static SecurityErrorResponse unauthorized(String code, String message, String path) {
        return new SecurityErrorResponse(401, code, message, path);
    }
}
