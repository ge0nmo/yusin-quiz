package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.file.controller.port.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class JsonBlockContentProcessor {
    private final FileService fileService;

    @Value("${cloud.aws.s3.prefix}")
    private String s3Prefix;

    public List<Map<String, Object>> withFreshImageUrls(List<Map<String, Object>> blocks) {
        return replaceManagedImageSources(blocks, fileService::generatePresignedUrl);
    }

    /**
     * 관리 이미지 src를 서명 URL 대신 object key로 바꾼다. 서명은 요청마다 달라지므로 콘텐츠 version 해시에 쓴다.
     */
    public List<Map<String, Object>> withImageObjectKeys(List<Map<String, Object>> blocks) {
        return replaceManagedImageSources(blocks, UnaryOperator.identity());
    }

    public boolean containsManagedImage(List<Map<String, Object>> blocks) {
        return blocks != null && blocks.stream().anyMatch(this::containsManagedImage);
    }

    private List<Map<String, Object>> replaceManagedImageSources(List<Map<String, Object>> blocks,
                                                                 UnaryOperator<String> sourceForObjectKey) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }
        return blocks.stream().map(block -> processMap(block, sourceForObjectKey)).toList();
    }

    private Map<String, Object> processMap(Map<String, Object> source, UnaryOperator<String> sourceForObjectKey) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key, processValue(value, sourceForObjectKey)));
        if ("image".equals(result.get("type")) && result.get("src") instanceof String src) {
            String objectKey = extractManagedObjectKey(src);
            if (objectKey != null) {
                String replacement = sourceForObjectKey.apply(objectKey);
                if (replacement != null && !replacement.isBlank()) {
                    result.put("src", replacement);
                }
            }
        }
        return result;
    }

    private Object processValue(Object value, UnaryOperator<String> sourceForObjectKey) {
        if (value instanceof Map<?, ?> map) {
            return processMap(normalize(map), sourceForObjectKey);
        }
        if (value instanceof List<?> list) {
            return list.stream().map(item -> processValue(item, sourceForObjectKey)).toList();
        }
        return value;
    }

    private boolean containsManagedImage(Object value) {
        if (value instanceof Map<?, ?> map) {
            if ("image".equals(map.get("type")) && map.get("src") instanceof String src
                    && extractManagedObjectKey(src) != null) {
                return true;
            }
            return map.values().stream().anyMatch(this::containsManagedImage);
        }
        if (value instanceof List<?> list) {
            return list.stream().anyMatch(this::containsManagedImage);
        }
        return false;
    }

    private Map<String, Object> normalize(Map<?, ?> map) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        map.forEach((key, nested) -> normalized.put(String.valueOf(key), nested));
        return normalized;
    }

    private String extractManagedObjectKey(String url) {
        try {
            URI uri = URI.create(url);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                return null;
            }
            String path = URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8);
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            return normalized.equals(s3Prefix) || normalized.startsWith(s3Prefix + "/") ? normalized : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
