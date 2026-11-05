package com.cpa.yusin.quiz.report.controller.dto;

import com.cpa.yusin.quiz.report.controller.dto.AdminReportDto.ReportUpdateRequest;
import com.cpa.yusin.quiz.report.domain.Report;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 관리자 처리 규칙. {@code answer}는 앞뒤 공백을 뺀 뒤 1000자 이하이고, 종료 상태면 1자 이상이어야 한다.
 * 실패하면 {@code VALIDATION_ERROR}의 {@code answer} 항목이 된다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidReportUpdateRequest.Validator.class)
public @interface ValidReportUpdateRequest {
    String message() default "답변을 확인해 주세요.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidReportUpdateRequest, ReportUpdateRequest> {
        @Override
        public boolean isValid(ReportUpdateRequest request, ConstraintValidatorContext context) {
            if (request == null) {
                return true;
            }
            String answer = request.answer() == null ? "" : request.answer().strip();
            String message = null;
            if (answer.length() > Report.MAX_ANSWER_LENGTH) {
                message = "답변은 1000자 이하로 적어 주세요.";
            } else if (request.status() != null && request.status().isClosed() && answer.isEmpty()) {
                message = "처리 완료나 반려에는 답변이 필요합니다.";
            }
            if (message == null) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addPropertyNode("answer").addConstraintViolation();
            return false;
        }
    }
}
