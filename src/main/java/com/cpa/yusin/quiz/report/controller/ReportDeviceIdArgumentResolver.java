package com.cpa.yusin.quiz.report.controller;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ReportException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Locale;
import java.util.regex.Pattern;

/** {@code X-Device-Id}가 없거나 UUID 형식(대소문자 무관, 하이픈 포함 36자)이 아니면 {@code 400 INVALID_DEVICE_ID}. */
@Component
public class ReportDeviceIdArgumentResolver implements HandlerMethodArgumentResolver {
    private static final Pattern UUID = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return ReportDeviceId.class.equals(parameter.getParameterType());
    }

    @Override
    public ReportDeviceId resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String value = webRequest.getHeader(ReportDeviceId.HEADER);
        if (value == null || !UUID.matcher(value.strip()).matches()) {
            throw new ReportException(ExceptionMessage.INVALID_DEVICE_ID);
        }
        return new ReportDeviceId(value.strip().toLowerCase(Locale.ROOT));
    }
}
