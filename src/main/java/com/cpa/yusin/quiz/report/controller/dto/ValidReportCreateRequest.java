package com.cpa.yusin.quiz.report.controller.dto;

import com.cpa.yusin.quiz.report.controller.dto.PublicReportDto.ReportCreateRequest;
import com.cpa.yusin.quiz.report.domain.Report;
import com.cpa.yusin.quiz.report.domain.ReportCategory;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.List;

/**
 * 유형에 따른 신고 요청 규칙. 실패하면 다른 Bean Validation 오류처럼 {@code VALIDATION_ERROR}가 된다.
 * <ul>
 *     <li>{@code content}는 앞뒤 공백을 뺀 뒤 1000자 이하, 내용 필수 유형이면 5자 이상</li>
 *     <li>문제 오류 유형은 {@code problemId} 필수</li>
 *     <li>{@code selectedChoiceNumbers}는 중복이 없고, 문제가 없으면 빈 배열</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidReportCreateRequest.Validator.class)
public @interface ValidReportCreateRequest {
    String message() default "신고 내용을 확인해 주세요.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidReportCreateRequest, ReportCreateRequest> {
        @Override
        public boolean isValid(ReportCreateRequest request, ConstraintValidatorContext context) {
            if (request == null) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            boolean valid = true;
            ReportCategory category = request.category();
            if (request.content() != null) {
                String content = request.content().strip();
                if (content.length() > Report.MAX_CONTENT_LENGTH) {
                    valid = violation(context, "content", "내용은 1000자 이하로 적어 주세요.");
                } else if (category != null && category.isContentRequired()
                        && content.length() < ReportCategory.MIN_REQUIRED_CONTENT_LENGTH) {
                    valid = violation(context, "content", "내용을 5자 이상 적어 주세요.");
                }
            }
            if (category != null && category.isProblemRequired() && request.problemId() == null) {
                valid = violation(context, "problemId", "문제 오류 신고에는 문제가 필요합니다.");
            }
            List<Integer> numbers = request.selectedChoiceNumbers();
            if (numbers != null) {
                if (new HashSet<>(numbers).size() != numbers.size()) {
                    valid = violation(context, "selectedChoiceNumbers", "고른 보기 번호가 중복되었습니다.");
                } else if (request.problemId() == null && !numbers.isEmpty()) {
                    valid = violation(context, "selectedChoiceNumbers", "문제가 없으면 고른 보기를 보낼 수 없습니다.");
                }
            }
            return valid;
        }

        private boolean violation(ConstraintValidatorContext context, String property, String message) {
            context.buildConstraintViolationWithTemplate(message).addPropertyNode(property).addConstraintViolation();
            return false;
        }
    }
}
