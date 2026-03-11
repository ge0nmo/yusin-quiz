package com.cpa.yusin.quiz.problem.controller.dto.response;

import com.cpa.yusin.quiz.problem.domain.Problem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemLectureResponse {

    private String youtubeUrl;
    private Integer startTimeSecond;
    private String playbackUrl;

    public static ProblemLectureResponse from(Problem problem) {
        if (problem == null) {
            return null;
        }

        String youtubeUrl = problem.getLectureYoutubeUrl();
        if (youtubeUrl == null || youtubeUrl.isBlank()) {
            return null;
        }

        Integer startTimeSecond = problem.getLectureStartSecond();

        return ProblemLectureResponse.builder()
                .youtubeUrl(youtubeUrl)
                .startTimeSecond(startTimeSecond)
                .playbackUrl(buildPlaybackUrl(youtubeUrl, startTimeSecond))
                .build();
    }

    private static String buildPlaybackUrl(String youtubeUrl, Integer startTimeSecond) {
        if (startTimeSecond == null) {
            return youtubeUrl;
        }

        String delimiter = youtubeUrl.contains("?") ? "&" : "?";
        return youtubeUrl + delimiter + "t=" + startTimeSecond + "s";
    }
}
