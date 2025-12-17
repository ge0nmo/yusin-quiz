package com.cpa.yusin.quiz.global.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice
{
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        log.error("컨트롤러 메서드 인자가 올바르지 않습니다.", e);

        return ErrorResponse.of(e.getBindingResult());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethod(MethodArgumentTypeMismatchException e)
    {
        log.error("컨트롤러 메서드 인자의 타입이 맞지 않습니다.", e);

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e)
    {
        log.error("검증 대상 객체가 제약을 위반했습니다.", e);

        return ErrorResponse.of(e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e)
    {
        log.error("지원하지 않는 HTTP 메서드입니다.", e);

        return ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
    {
        log.error("요청에 필요한 body 가 없습니다.", e);

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, "Required request body is missing");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e)
    {
        log.error("요청에 필요한 파라미터가 없습니다: " + e.getMessage() , e);

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoSuchElementException(NoSuchElementException e)
    {
        log.error("요소를 찾을 수 없습니다: " + e.getMessage(), e);

        return ErrorResponse.of(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateKeyException(DuplicateKeyException e)
    {
        log.error("해당 키가 중복입니다: " + e.getMessage(), e);

        return ErrorResponse.of(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e)
    {
        log.error("예외가 발생했습니다.", e);

        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIOException(IOException e)
    {
        log.error("입출력 예외가 발생했습니다.", e);

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        // 필요한 경우, 로그를 남기고, 404 상태 코드와 함께 응답
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("요청한 경로를 찾을 수 없습니다.");
    }

    // ExceptionAdvice.java에 추가
    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleExpiredJwtException(io.jsonwebtoken.ExpiredJwtException e) {
        log.error("토큰 만료 예외 발생", e);
        return ErrorResponse.of(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.");
    }

    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleJwtException(io.jsonwebtoken.JwtException e) {
        log.error("JWT 예외 발생", e);
        return ErrorResponse.of(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
    }
}
