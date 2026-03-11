package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemLectureRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YoutubeLectureUrlProcessorTest {

    private final YoutubeLectureUrlProcessor youtubeLectureUrlProcessor = new YoutubeLectureUrlProcessor();

    @Test
    void normalize_whenShortYoutubeUrl_thenReturnsCanonicalWatchUrl() {
        ProblemLectureRequest request = ProblemLectureRequest.builder()
                .youtubeUrl("https://youtu.be/abc123XYZ09?t=430")
                .startTimeSecond(430)
                .build();

        YoutubeLectureUrlProcessor.NormalizedYoutubeLecture result = youtubeLectureUrlProcessor.normalize(request);

        assertThat(result.canonicalYoutubeUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123XYZ09");
        assertThat(result.startTimeSecond()).isEqualTo(430);
    }

    @Test
    void normalize_whenWatchUrlAndNullStartTime_thenAllowsLectureOnlyMode() {
        ProblemLectureRequest request = ProblemLectureRequest.builder()
                .youtubeUrl("https://www.youtube.com/watch?v=abc123XYZ09&si=test")
                .startTimeSecond(null)
                .build();

        YoutubeLectureUrlProcessor.NormalizedYoutubeLecture result = youtubeLectureUrlProcessor.normalize(request);

        assertThat(result.canonicalYoutubeUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123XYZ09");
        assertThat(result.startTimeSecond()).isNull();
    }

    @Test
    void normalize_whenYoutubeUrlIsMissing_thenThrowsBadRequest() {
        ProblemLectureRequest request = ProblemLectureRequest.builder()
                .startTimeSecond(430)
                .build();

        assertThatThrownBy(() -> youtubeLectureUrlProcessor.normalize(request))
                .isInstanceOf(ProblemException.class)
                .hasMessage("유효한 유튜브 해설 링크를 입력해야 합니다.");
    }

    @Test
    void normalize_whenStartTimeIsNegative_thenThrowsBadRequest() {
        ProblemLectureRequest request = ProblemLectureRequest.builder()
                .youtubeUrl("https://www.youtube.com/watch?v=abc123XYZ09")
                .startTimeSecond(-1)
                .build();

        assertThatThrownBy(() -> youtubeLectureUrlProcessor.normalize(request))
                .isInstanceOf(ProblemException.class)
                .hasMessage("해설강의 시작 시간은 0 이상이어야 합니다.");
    }

    @Test
    void normalize_whenNonYoutubeUrl_thenThrowsBadRequest() {
        ProblemLectureRequest request = ProblemLectureRequest.builder()
                .youtubeUrl("https://example.com/watch?v=abc123XYZ09")
                .build();

        assertThatThrownBy(() -> youtubeLectureUrlProcessor.normalize(request))
                .isInstanceOf(ProblemException.class)
                .hasMessage("유효한 유튜브 해설 링크를 입력해야 합니다.");
    }
}
