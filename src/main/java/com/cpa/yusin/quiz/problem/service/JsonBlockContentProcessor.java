package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.file.controller.port.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JsonBlockContentProcessor {
    private final FileService fileService;

    @Value("${cloud.aws.s3.prefix}")
    private String s3Prefix;

    public List<Map<String, Object>> withFreshImageUrls(List<Map<String, Object>> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }
        return blocks.stream().map(this::processMap).toList();
    }

    private Map<String, Object> processMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key, processValue(value)));
        if ("image".equals(result.get("type")) && result.get("src") instanceof String src) {
            String objectKey = extractManagedObjectKey(src);
            if (objectKey != null) {
                String freshUrl = fileService.generatePresignedUrl(objectKey);
                if (freshUrl != null && !freshUrl.isBlank()) {
                    result.put("src", freshUrl);
                }
            }
        }
        return result;
    }

    private Object processValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            map.forEach((key, nested) -> normalized.put(String.valueOf(key), nested));
            return processMap(normalized);
        }
        if (value instanceof List<?> list) {
            return list.stream().map(this::processValue).toList();
        }
        return value;
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
