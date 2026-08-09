package com.cpa.yusin.quiz.content.controller.dto;

import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

public final class AdminContentDto {
    private AdminContentDto() {}

    public record MappingRequest(@NotNull Long subjectId, @NotNull ContentStatus status,
                                 @PositiveOrZero int displayOrder) {}
    public record MappingResponse(Long id, Long subjectId, String subjectName,
                                  ContentStatus status, int displayOrder, long problemCount) {}
    public record QualificationExamCreateRequest(@NotNull QualificationExamCode code,
                                                 @NotNull ContentStatus status,
                                                 @NotNull List<@Valid MappingRequest> subjects) {}
    public record QualificationExamUpdateRequest(@NotNull ContentStatus status,
                                                 @NotNull List<@Valid MappingRequest> subjects) {}
    public record QualificationExamResponse(Long id, QualificationExamCode code, String name, ContentStatus status,
                                            List<MappingResponse> subjects) {}

    public record SubjectRequest(@NotBlank String name, @NotNull ContentStatus status) {}
    public record SubjectResponse(Long id, String name, ContentStatus status) {}

    public record ExamRequest(@NotNull Long qualificationExamId, @NotBlank String name,
                              @Min(1900) @Max(3000) int year, @NotNull ContentStatus status) {}
    public record ExamResponse(Long id, Long qualificationExamId, QualificationExamCode qualificationExamCode,
                               String qualificationExamName, String name, int year,
                               ContentStatus status, long problemCount) {}

    public record ChoiceRequest(Long id, @Min(1) @Max(5) int number, @NotBlank String content,
                                boolean isAnswer, List<Map<String, Object>> explanation) {}
    public record ProblemRequest(@NotNull Long examId, @NotNull Long subjectId, @Positive int number,
                                 @NotNull ContentStatus status,
                                 @NotEmpty List<Map<String, Object>> content,
                                 List<Map<String, Object>> explanation,
                                 @NotNull @Size(min = 5, max = 5) List<@Valid ChoiceRequest> choices) {}
    public record ChoiceDetailResponse(Long id, int number, String content, boolean isAnswer,
                                       List<Map<String, Object>> explanation) {}
    public record ProblemDetailResponse(Long id, Long examId, Long subjectId, int number,
                                        ContentStatus status, List<Map<String, Object>> content,
                                        List<Map<String, Object>> explanation,
                                        List<ChoiceDetailResponse> choices) {}
    public record ProblemSummaryResponse(Long id, Long examId, String examName, int examYear,
                                         Long qualificationExamId, String qualificationExamName,
                                         Long subjectId, String subjectName, int number,
                                         ContentStatus status, String contentPreviewText,
                                         int choiceCount, long answerChoiceCount) {}
    public record NextProblemNumberResponse(int nextNumber) {}

    public record DashboardResponse(long qualificationExamCount, long subjectCount,
                                    long examCount, long problemCount) {}
}
