package com.cpa.yusin.quiz.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionMessage {
    INVALID_LOGIN_INFORMATION("로그인 정보가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("유효한 관리자 리프레시 토큰이 아닙니다.", HttpStatus.UNAUTHORIZED),
    NO_AUTHORIZATION("권한이 없습니다.", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("관리자 계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    QUALIFICATION_EXAM_NOT_FOUND("자격시험을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    QUALIFICATION_EXAM_CODE_EXISTS("이미 등록된 자격시험입니다.", HttpStatus.CONFLICT),
    SUBJECT_NOT_FOUND("과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SUBJECT_NAME_EXISTS("이미 사용 중인 과목 이름입니다.", HttpStatus.CONFLICT),
    SUBJECT_MAPPING_NOT_FOUND("자격시험에 연결된 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    EXAM_NOT_FOUND("시험 회차를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    EXAM_DUPLICATED("같은 자격시험에 동일한 연도와 회차명이 이미 존재합니다.", HttpStatus.CONFLICT),
    PROBLEM_NOT_FOUND("문제를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PROBLEM_NUMBER_EXISTS("같은 시험 회차와 과목에 동일한 문제 번호가 이미 존재합니다.", HttpStatus.CONFLICT),
    CHOICE_NOT_FOUND("보기를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PROBLEM_CHOICES("보기는 1번부터 5번까지 정확히 다섯 개이며 정답은 하나여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_STATEMENT_GROUP("지문 묶음의 라벨과 내용을 확인해 주세요.", HttpStatus.BAD_REQUEST),
    INVALID_SUBJECT_MAPPING("문제 과목은 시험 자격시험에 연결된 과목이어야 합니다.", HttpStatus.BAD_REQUEST),
    CONTENT_IN_USE("사용 중인 콘텐츠는 삭제할 수 없습니다.", HttpStatus.CONFLICT),
    INVALID_DATA("유효하지 않은 데이터입니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;

    ExceptionMessage(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
