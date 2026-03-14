package com.cpa.yusin.quiz.global.logging;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Array;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LogUtils {

    private static final int MAX_LOG_LENGTH = 1000; // 로그 최대 길이 제한

    // 객체 정보를 안전하게 문자열로 변환 (길이 제한 적용)
    public static String toSimpleString(Object arg) {
        if (arg == null) {
            return "null";
        }

        try {
            return summarize(arg);
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

    private static String summarize(Object arg) {
        if (isSimpleScalar(arg)) {
            return truncate(arg.toString());
        }

        if (arg instanceof ResponseEntity<?> responseEntity) {
            return String.format("ResponseEntity(status=%s, bodyType=%s)",
                    responseEntity.getStatusCode(),
                    simpleTypeName(responseEntity.getBody()));
        }

        if (arg instanceof GlobalResponse<?> globalResponse) {
            return String.format("GlobalResponse(data=%s, pageInfo=%s)",
                    summarizeGlobalResponseData(globalResponse.getData()),
                    globalResponse.getPageInfo() == null ? "absent" : "present");
        }

        if (arg instanceof Page<?> page) {
            return String.format("Page(size=%d, elementType=%s)",
                    page.getNumberOfElements(),
                    findElementType(page.getContent()));
        }

        if (arg instanceof Slice<?> slice) {
            return String.format("Slice(size=%d, elementType=%s)",
                    slice.getNumberOfElements(),
                    findElementType(slice.getContent()));
        }

        if (arg instanceof Collection<?> collection) {
            return String.format("%s(size=%d, elementType=%s)",
                    collectionTypeName(collection),
                    collection.size(),
                    findElementType(collection));
        }

        if (arg.getClass().isArray()) {
            return summarizeArray(arg);
        }

        return simpleTypeName(arg);
    }

    private static boolean isSimpleScalar(Object arg) {
        return arg instanceof CharSequence
                || arg instanceof Number
                || arg instanceof Boolean
                || arg instanceof Character
                || arg instanceof Enum<?>
                || arg instanceof TemporalAccessor;
    }

    private static String summarizeGlobalResponseData(Object data) {
        if (data == null) {
            return "null";
        }

        if (data instanceof Page<?> page) {
            return String.format("Page(size=%d, elementType=%s)",
                    page.getNumberOfElements(),
                    findElementType(page.getContent()));
        }

        if (data instanceof Slice<?> slice) {
            return String.format("Slice(size=%d, elementType=%s)",
                    slice.getNumberOfElements(),
                    findElementType(slice.getContent()));
        }

        if (data instanceof Collection<?> collection) {
            return String.format("%s(size=%d, elementType=%s)",
                    collectionTypeName(collection),
                    collection.size(),
                    findElementType(collection));
        }

        if (data.getClass().isArray()) {
            return summarizeArray(data);
        }

        return simpleTypeName(data);
    }

    private static String summarizeArray(Object array) {
        int length = Array.getLength(array);
        Class<?> componentType = array.getClass().getComponentType();

        if (componentType != null && componentType.isPrimitive()) {
            return String.format("%s[](length=%d)", componentType.getSimpleName(), length);
        }

        List<?> elements = Arrays.asList((Object[]) array);
        return String.format("Array(length=%d, elementType=%s)", length, findElementType(elements));
    }

    private static String collectionTypeName(Collection<?> collection) {
        if (collection instanceof List<?>) {
            return "List";
        }
        return collection.getClass().getSimpleName();
    }

    private static String findElementType(Collection<?> collection) {
        return collection.stream()
                .filter(element -> element != null)
                .map(LogUtils::simpleTypeName)
                .findFirst()
                .orElse("Unknown");
    }

    private static String simpleTypeName(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName();
    }

    private static String truncate(String value) {
        if (value.length() > MAX_LOG_LENGTH) {
            return value.substring(0, MAX_LOG_LENGTH) + "...(truncated)";
        }
        return value;
    }
}
