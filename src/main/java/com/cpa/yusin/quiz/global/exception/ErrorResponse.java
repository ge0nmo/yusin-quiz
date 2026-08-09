package com.cpa.yusin.quiz.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final int status;
    private final String message;
    private final String code;
    private List<ValueError> valueErrors;

    private ErrorResponse(int status, String message) {
        this(status, message, HttpStatus.valueOf(status).name());
    }

    private ErrorResponse(int status, String message, String code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    private ErrorResponse(final List<ValueError> valueErrors) {
        this.status = HttpStatus.BAD_REQUEST.value();
        this.message = HttpStatus.BAD_REQUEST.getReasonPhrase();
        this.code = "VALIDATION_ERROR";
        this.valueErrors = valueErrors;
    }

    public static ErrorResponse of(HttpStatus httpStatus) {
        return new ErrorResponse(httpStatus.value(), httpStatus.getReasonPhrase());
    }

    public static ErrorResponse of(HttpStatus httpStatus, String message) {
        return new ErrorResponse(httpStatus.value(), message);
    }

    public static ErrorResponse of(HttpStatus httpStatus, String message, String code) {
        return new ErrorResponse(httpStatus.value(), message, code);
    }

    public static ErrorResponse of(ExceptionMessage exceptionMessage) {
        return new ErrorResponse(
                exceptionMessage.getHttpStatus().value(),
                exceptionMessage.getMessage(),
                exceptionMessage.name());
    }

    public static ErrorResponse of(BindingResult bindingResult) {
        return new ErrorResponse(ValueError.of(bindingResult));
    }

    public static ErrorResponse of(ConstraintViolationException e) {
        return new ErrorResponse(ValueError.of(e));
    }

    @Getter
    public static class ValueError {
        private final String descriptor;
        private final Object rejectedValue;
        private final String reason;

        private ValueError(String descriptor, Object rejectedValue, String reason) {
            this.descriptor = descriptor;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public static List<ValueError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> new ValueError(
                            normalizeDescriptor(error.getField()),
                            sanitizeRejectedValue(error.getField(), error.getRejectedValue()),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }

        public static List<ValueError> of(ConstraintViolationException e) {
            return e.getConstraintViolations()
                    .stream()
                    .map(error -> new ValueError(
                            normalizeDescriptor(error.getPropertyPath().toString()),
                            sanitizeRejectedValue(error.getPropertyPath().toString(), error.getInvalidValue()),
                            error.getMessage()))
                    .collect(Collectors.toList());
        }

        private static String normalizeDescriptor(String descriptor) {
            return descriptor.replace(".<list element>", "");
        }

        private static Object sanitizeRejectedValue(String descriptor, Object rejectedValue) {
            String normalizedDescriptor = descriptor.toLowerCase();
            if (normalizedDescriptor.contains("token")
                    || normalizedDescriptor.contains("password")
                    || normalizedDescriptor.contains("secret")) {
                return "[REDACTED]";
            }

            return rejectedValue == null ? "" : rejectedValue.toString();
        }
    }
}
