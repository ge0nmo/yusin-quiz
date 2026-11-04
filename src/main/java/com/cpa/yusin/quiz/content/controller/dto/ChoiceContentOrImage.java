package com.cpa.yusin.quiz.content.controller.dto;

import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.ChoiceRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 보기는 본문(content)이 비어 있지 않거나 이미지(imageUrl)가 있어야 한다.
 * <p>
 * 그림만 있는 보기가 생기기 전의 {@code @NotBlank content}와 같은 오류(VALIDATION_ERROR, {@code choices[i].content})를 낸다.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ChoiceContentOrImage.Validator.class)
public @interface ChoiceContentOrImage {
    String message() default "{jakarta.validation.constraints.NotBlank.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ChoiceContentOrImage, ChoiceRequest> {
        @Override
        public boolean isValid(ChoiceRequest choice, ConstraintValidatorContext context) {
            if (choice == null || hasText(choice.content()) || hasText(choice.imageUrl())) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("content")
                    .addConstraintViolation();
            return false;
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
