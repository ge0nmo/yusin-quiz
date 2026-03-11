package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemLectureRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class YoutubeLectureUrlProcessor {

    public NormalizedYoutubeLecture normalize(ProblemLectureRequest lectureRequest) {
        if (lectureRequest == null) {
            return null;
        }

        String rawYoutubeUrl = normalizeBlank(lectureRequest.getYoutubeUrl());
        if (rawYoutubeUrl == null) {
            throw new ProblemException(ExceptionMessage.INVALID_PROBLEM_LECTURE_URL);
        }

        Integer startTimeSecond = lectureRequest.getStartTimeSecond();
        if (startTimeSecond != null && startTimeSecond < 0) {
            throw new ProblemException(ExceptionMessage.INVALID_PROBLEM_LECTURE_START_TIME);
        }

        String canonicalYoutubeUrl = buildCanonicalYoutubeUrl(rawYoutubeUrl);

        return new NormalizedYoutubeLecture(canonicalYoutubeUrl, startTimeSecond);
    }

    private String buildCanonicalYoutubeUrl(String rawYoutubeUrl) {
        try {
            URI uri = new URI(ensureScheme(rawYoutubeUrl));
            String host = Optional.ofNullable(uri.getHost())
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new ProblemException(ExceptionMessage.INVALID_PROBLEM_LECTURE_URL));

            String videoId = switch (host) {
                case "youtu.be" -> extractVideoIdFromShortUri(uri);
                case "youtube.com", "www.youtube.com" -> extractVideoIdFromYoutubeUri(uri);
                default -> throw new ProblemException(ExceptionMessage.INVALID_PROBLEM_LECTURE_URL);
            };

            if (videoId == null || videoId.isBlank()) {
                throw new ProblemException(ExceptionMessage.INVALID_PROBLEM_LECTURE_URL);
            }

            return "https://www.youtube.com/watch?v=" + videoId;
        } catch (URISyntaxException e) {
            throw new ProblemException(ExceptionMessage.INVALID_PROBLEM_LECTURE_URL);
        }
    }

    private String extractVideoIdFromShortUri(URI uri) {
        return Arrays.stream(uri.getPath().split("/"))
                .filter(segment -> !segment.isBlank())
                .findFirst()
                .orElseThrow(() -> new ProblemException(ExceptionMessage.INVALID_PROBLEM_LECTURE_URL));
    }

    private String extractVideoIdFromYoutubeUri(URI uri) {
        String normalizedPath = Optional.ofNullable(uri.getPath()).orElse("");
        Map<String, String> queryParameters = parseQueryParameters(uri.getRawQuery());

        if (Objects.equals("/watch", normalizedPath)) {
            return normalizeBlank(queryParameters.get("v"));
        }

        if (normalizedPath.startsWith("/shorts/")) {
            return normalizeBlank(normalizedPath.substring("/shorts/".length()));
        }

        if (normalizedPath.startsWith("/embed/")) {
            return normalizeBlank(normalizedPath.substring("/embed/".length()));
        }

        return null;
    }

    private Map<String, String> parseQueryParameters(String rawQuery) {
        Map<String, String> queryParameters = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return queryParameters;
        }

        for (String queryParameter : rawQuery.split("&")) {
            String[] keyValue = queryParameter.split("=", 2);
            if (keyValue.length == 2) {
                queryParameters.put(keyValue[0], keyValue[1]);
            }
        }

        return queryParameters;
    }

    private String ensureScheme(String rawYoutubeUrl) {
        if (rawYoutubeUrl.startsWith("http://") || rawYoutubeUrl.startsWith("https://")) {
            return rawYoutubeUrl;
        }

        return "https://" + rawYoutubeUrl;
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    public record NormalizedYoutubeLecture(String canonicalYoutubeUrl, Integer startTimeSecond) {
    }
}
