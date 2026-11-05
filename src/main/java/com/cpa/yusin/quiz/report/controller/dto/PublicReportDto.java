package com.cpa.yusin.quiz.report.controller.dto;

import com.cpa.yusin.quiz.report.domain.ReportCategory;
import com.cpa.yusin.quiz.report.domain.ReportStatus;
import com.cpa.yusin.quiz.version.domain.AppPlatform;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public final class PublicReportDto {
    private PublicReportDto() {}

    /**
     * 앱의 신고·문의. 유형별 문제·내용 규칙과 고른 보기 규칙은 {@link ValidReportCreateRequest}가 검사한다.
     */
    @ValidReportCreateRequest
    public record ReportCreateRequest(@NotNull ReportCategory category,
                                      Long problemId,
                                      @NotNull String content,
                                      @NotNull @Size(max = 5) List<@NotNull @Min(1) @Max(5) Integer> selectedChoiceNumbers,
                                      @NotBlank @Size(max = 20) String appVersion,
                                      @NotNull AppPlatform platform) {}

    /** 작성 시점의 문제 스냅숏. 문제가 삭제되면 id만 null이다. */
    public record ReportProblemResponse(Long id, Integer number, String examName, Integer examYear,
                                        String subjectName) {}

    /** answer와 answeredAt은 종료 상태일 때만 값이 있다. */
    public record MyReportResponse(Long id, ReportCategory category, ReportStatus status,
                                   ReportProblemResponse problem, String content,
                                   List<Integer> selectedChoiceNumbers, String answer, LocalDateTime answeredAt,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {}
}
