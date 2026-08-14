package com.cpa.yusin.quiz.content.controller.dto;

import java.util.List;
import java.util.Map;

public final class PublicContentDto {
    private PublicContentDto() {}

    public record SubjectResponse(Long id, String name, long problemCount) {}
    public record ExamResponse(Long id, String name, int year) {}
    public record ChoiceResponse(Long id, int number, String content) {}
    public record ProblemResponse(Long id, int number, List<Map<String, Object>> content,
                                  ExamResponse exam, List<ChoiceResponse> choices) {}
    public record CheckRequest(Long selectedChoiceId) {}
    public record CheckResponse(boolean correct) {}
    public record SolutionsRequest(List<Long> problemIds) {}
    public record ChoiceSolutionResponse(Long choiceId, List<Map<String, Object>> explanation) {}
    /**
     * 문제 해설 응답 DTO.
     * @param correctChoiceIds 해당 문제 정답 보기 ID 리스트 (복수 정답 대응)
     */
    public record SolutionResponse(Long problemId, List<Long> correctChoiceIds,
                                   List<Map<String, Object>> explanation,
                                   List<ChoiceSolutionResponse> choices) {}
}
