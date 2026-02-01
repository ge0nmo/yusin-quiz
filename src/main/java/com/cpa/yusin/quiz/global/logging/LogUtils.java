package com.cpa.yusin.quiz.global.logging;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class LogUtils {
    
    private static final int MAX_LOG_LENGTH = 1000; // 로그 최대 길이 제한

    // 객체 정보를 안전하게 문자열로 변환 (길이 제한 적용)
    public static String toSimpleString(Object arg) {
        if (arg == null) return "null";
        try {
            String str = arg.toString();
            if (str.length() > MAX_LOG_LENGTH) {
                return str.substring(0, MAX_LOG_LENGTH) + "...(truncated)";
            }
            return str;
        } catch (Exception e) {
            return "ConvertError";
        }
    }

    // 민감 정보가 포함될 수 있는 args 배열 처리
    public static String argsToString(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
                .map(LogUtils::toSimpleString)
                .collect(Collectors.joining(", "));
    }
}