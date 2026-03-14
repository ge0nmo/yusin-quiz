package com.cpa.yusin.quiz.dashboard.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.dashboard.controller.dto.response.DashboardContextResponse;
import com.cpa.yusin.quiz.dashboard.controller.dto.response.DashboardExamContextResponse;
import com.cpa.yusin.quiz.dashboard.controller.dto.response.DashboardOperationsResponse;
import com.cpa.yusin.quiz.dashboard.controller.dto.response.DashboardPendingQuestionResponse;
import com.cpa.yusin.quiz.dashboard.controller.dto.response.DashboardResponse;
import com.cpa.yusin.quiz.dashboard.controller.dto.response.DashboardSubjectContextResponse;
import com.cpa.yusin.quiz.dashboard.controller.dto.response.DashboardTotalsResponse;
import com.cpa.yusin.quiz.dashboard.controller.port.DashboardService;
import com.cpa.yusin.quiz.dashboard.service.port.DashboardExamContextProjection;
import com.cpa.yusin.quiz.dashboard.service.port.DashboardPendingQuestionProjection;
import com.cpa.yusin.quiz.dashboard.service.port.DashboardRepository;
import com.cpa.yusin.quiz.dashboard.service.port.DashboardSubjectContextProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DashboardServiceImpl implements DashboardService {

    // The dashboard only exposes the latest 5 pending questions to keep the payload
    // small and aligned with the admin card layout.
    private static final int PENDING_QUESTION_LIMIT = 5;

    private final DashboardRepository dashboardRepository;
    private final ClockHolder clockHolder;

    @Override
    public DashboardResponse getDashboard(Long subjectId, Long examId) {
        // We derive the day boundary from ClockHolder so time-sensitive logic stays
        // testable and follows the project's existing time abstraction.
        LocalDateTime startOfToday = clockHolder.getCurrentDateTime().toLocalDate().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        // Global totals are always computed from the active dataset, regardless of
        // the currently selected subject/exam filters in the dashboard UI.
        DashboardTotalsResponse totals = new DashboardTotalsResponse(
                dashboardRepository.countActiveSubjects(),
                dashboardRepository.countActiveExams(),
                dashboardRepository.countActiveProblems(),
                dashboardRepository.countActiveQuestions()
        );

        // Operational metrics are also global dashboard signals. The repository layer
        // owns the soft-delete propagation rules so this service stays persistence-agnostic.
        DashboardOperationsResponse operations = new DashboardOperationsResponse(
                dashboardRepository.countTodayQuestions(startOfToday, startOfTomorrow),
                dashboardRepository.countUnansweredQuestions(),
                dashboardRepository.countProblemsWithoutLecture()
        );

        // The pending list intentionally shows only the latest unanswered questions,
        // already pre-sorted by the repository contract.
        List<DashboardPendingQuestionResponse> pendingQuestions = dashboardRepository.findPendingQuestions(PENDING_QUESTION_LIMIT)
                .stream()
                .map(this::toPendingQuestionResponse)
                .toList();

        // Stale selections are not treated as errors. Returning null context lets the
        // admin UI recover gracefully without breaking the whole dashboard request.
        DashboardContextResponse context = new DashboardContextResponse(
                subjectId == null ? null : dashboardRepository.findSubjectContext(subjectId)
                        .map(this::toSubjectContextResponse)
                        .orElse(null),
                examId == null ? null : dashboardRepository.findExamContext(examId, subjectId)
                        .map(this::toExamContextResponse)
                        .orElse(null)
        );

        return new DashboardResponse(totals, operations, pendingQuestions, context);
    }

    private DashboardPendingQuestionResponse toPendingQuestionResponse(DashboardPendingQuestionProjection projection) {
        // Keep controller DTO mapping in the service boundary so repository projections
        // do not leak directly to the API layer.
        return new DashboardPendingQuestionResponse(
                projection.id(),
                projection.title(),
                projection.username(),
                projection.createdAt(),
                projection.answerCount(),
                projection.problemId()
        );
    }

    private DashboardSubjectContextResponse toSubjectContextResponse(DashboardSubjectContextProjection projection) {
        // Subject context is a lightweight summary for the selected card, not a full
        // subject detail response.
        return new DashboardSubjectContextResponse(
                projection.id(),
                projection.name(),
                projection.examCount(),
                projection.problemCount()
        );
    }

    private DashboardExamContextResponse toExamContextResponse(DashboardExamContextProjection projection) {
        // Exam context folds both raw counts and a derived coverage ratio into a single
        // response object so the controller does not perform view-specific calculations.
        return new DashboardExamContextResponse(
                projection.id(),
                projection.name(),
                projection.year(),
                projection.problemCount(),
                projection.questionCount(),
                projection.unansweredQuestionCount(),
                calculateLectureCoverageRate(projection.problemsWithLectureCount(), projection.problemCount())
        );
    }

    private double calculateLectureCoverageRate(long problemsWithLectureCount, long problemCount) {
        // No active problems means there is no meaningful denominator, so we return 0
        // instead of propagating divide-by-zero or NaN into the API contract.
        if (problemCount == 0L) {
            return 0.0d;
        }

        // We round to one decimal place to keep the admin metric stable and predictable
        // for UI rendering while still preserving useful precision.
        return BigDecimal.valueOf(problemsWithLectureCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(problemCount), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
