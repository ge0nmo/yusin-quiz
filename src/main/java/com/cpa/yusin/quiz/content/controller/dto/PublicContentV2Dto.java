package com.cpa.yusin.quiz.content.controller.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class PublicContentV2Dto {
    private PublicContentV2Dto() {}

    /**
     * 앱 시작 시 내려받는 문제·정답 전체 스냅샷. 해설은 이용권 적용 대상이라 {@link ExplanationsResponse}로 분리한다.
     */
    public record ContentResponse(String qualificationExamCode, String version, Instant generatedAt,
                                  Instant imageUrlsExpireAt, List<SubjectContentResponse> subjects) {}
    public record SubjectContentResponse(Long id, String name, long problemCount,
                                         List<ProblemContentResponse> problems) {}
    public record ExamResponse(Long id, String name, int year) {}
    public record ChoiceResponse(Long id, int number, String content) {}
    public record ProblemContentResponse(Long id, int number, ExamResponse exam, List<Map<String, Object>> content,
                                         List<Long> correctChoiceIds, List<ChoiceResponse> choices) {}

    public record ExplanationsResponse(String qualificationExamCode, String version, Instant generatedAt,
                                       Instant imageUrlsExpireAt, List<ProblemExplanationResponse> problems) {}
    public record ChoiceExplanationResponse(Long choiceId, List<Map<String, Object>> explanation) {}
    public record ProblemExplanationResponse(Long problemId, List<Map<String, Object>> explanation,
                                             List<ChoiceExplanationResponse> choices) {}
}
