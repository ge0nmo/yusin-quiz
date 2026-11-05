package com.cpa.yusin.quiz.report.controller.dto;

import com.cpa.yusin.quiz.report.domain.ReportCategory;
import com.cpa.yusin.quiz.report.domain.ReportStatus;
import com.cpa.yusin.quiz.version.domain.AppPlatform;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class AdminReportDto {
    private AdminReportDto() {}

    public record ReportQualificationExamResponse(Long id, String name) {}

    /**
     * 번호·회차·연도·과목 이름은 작성 시점 스냅숏, id·examId·subjectId는 현재 문제 값이다. 문제가 삭제되면 세 id가 null이다.
     */
    public record AdminReportProblemResponse(Long id, Integer number, Long examId, String examName, Integer examYear,
                                             Long subjectId, String subjectName) {}

    public record AdminReportSummaryResponse(Long id, ReportStatus status, ReportCategory category,
                                             ReportQualificationExamResponse qualificationExam,
                                             AdminReportProblemResponse problem, String content,
                                             long sameProblemOpenCount, LocalDateTime createdAt) {}

    public record SameProblemReportResponse(Long id, ReportStatus status, ReportCategory category, String content,
                                            LocalDateTime createdAt) {}

    public record AdminReportDetailResponse(Long id, ReportStatus status, ReportCategory category,
                                            ReportQualificationExamResponse qualificationExam,
                                            AdminReportProblemResponse problem, String content,
                                            long sameProblemOpenCount, LocalDateTime createdAt,
                                            List<Integer> selectedChoiceNumbers, String appVersion,
                                            AppPlatform platform, String deviceIdMasked, long deviceReportCount,
                                            long deviceRejectedCount, String answer, LocalDateTime answeredAt,
                                            LocalDateTime updatedAt,
                                            List<SameProblemReportResponse> sameProblemOpenReports) {}

    public record ReportCountResponse(long openCount, long resolvedCount, long rejectedCount, long totalCount) {}

    /** 종료 상태(RESOLVED/REJECTED)는 답변이 필요하다. 규칙은 {@link ValidReportUpdateRequest}가 검사한다. */
    @ValidReportUpdateRequest
    public record ReportUpdateRequest(@NotNull ReportStatus status, String answer, Boolean applyToSameProblemOpen) {
        public boolean applyToSameProblem() {
            return Boolean.TRUE.equals(applyToSameProblemOpen);
        }
    }

    /** 처리한 신고 id. 요청 대상이 첫 번째다. */
    public record ReportUpdateResponse(List<Long> updatedIds) {}
}
