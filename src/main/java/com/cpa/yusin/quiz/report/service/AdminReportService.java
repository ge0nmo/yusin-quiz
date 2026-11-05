package com.cpa.yusin.quiz.report.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ReportException;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.report.controller.dto.AdminReportDto.*;
import com.cpa.yusin.quiz.report.domain.*;
import com.cpa.yusin.quiz.report.infrastructure.ReportRepository;
import com.cpa.yusin.quiz.report.infrastructure.ReportRepository.ProblemCount;
import com.cpa.yusin.quiz.report.infrastructure.ReportRepository.StatusCount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** 관리자 신고·문의 조회와 처리. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {
    private final ReportRepository reportRepository;
    private final ClockHolder clockHolder;

    /** {@code page}는 1부터 센다. */
    public Page<AdminReportSummaryResponse> getReports(ReportStatusGroup statusGroup, Long qualificationExamId,
                                                       ReportCategoryGroup categoryGroup, ReportSort sort,
                                                       int page, int size) {
        List<ReportStatus> statuses = statusGroup.statuses();
        List<ReportCategory> categories = ReportCategory.of(categoryGroup);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Report> reports = switch (sort) {
            case SAME_PROBLEM -> reportRepository.searchBySameProblem(statuses, categories, qualificationExamId, pageable);
            case LATEST -> reportRepository.searchLatest(statuses, categories, qualificationExamId, pageable);
            case OLDEST -> reportRepository.searchOldest(statuses, categories, qualificationExamId, pageable);
        };
        Map<Long, Long> openCounts = openCountsByProblem(reports.getContent());
        return reports.map(report -> toSummary(report, openCount(openCounts, report)));
    }

    public ReportCountResponse getCounts(Long qualificationExamId) {
        Map<ReportStatus, Long> counts = new EnumMap<>(ReportStatus.class);
        for (StatusCount count : reportRepository.countByStatus(qualificationExamId)) {
            counts.put(count.status(), count.count());
        }
        long open = ReportStatus.OPEN.stream().mapToLong(status -> counts.getOrDefault(status, 0L)).sum();
        long resolved = counts.getOrDefault(ReportStatus.RESOLVED, 0L);
        long rejected = counts.getOrDefault(ReportStatus.REJECTED, 0L);
        return new ReportCountResponse(open, resolved, rejected, open + resolved + rejected);
    }

    public long countOpen() {
        return reportRepository.countByStatusIn(ReportStatus.OPEN);
    }

    public AdminReportDetailResponse getReport(Long id) {
        Report report = findReport(id);
        List<Report> sameProblemOpen = sameProblemOpenReports(report);
        long sameProblemOpenCount = report.getProblem() == null ? 0
                : sameProblemOpen.size() + (report.getStatus().isOpen() ? 1 : 0);
        AdminReportSummaryResponse summary = toSummary(report, sameProblemOpenCount);
        return new AdminReportDetailResponse(summary.id(), summary.status(), summary.category(),
                summary.qualificationExam(), summary.problem(), summary.content(), summary.sameProblemOpenCount(),
                summary.createdAt(), report.getSelectedChoiceNumbers(), report.getAppVersion(), report.getPlatform(),
                mask(report.getDeviceId()), reportRepository.countByDeviceId(report.getDeviceId()),
                reportRepository.countByDeviceIdAndStatus(report.getDeviceId(), ReportStatus.REJECTED),
                report.getAnswer(), ReportTimes.of(report.getAnsweredAt()), ReportTimes.of(report.getUpdatedAt()),
                sameProblemOpen.stream()
                        .map(other -> new SameProblemReportResponse(other.getId(), other.getStatus(),
                                other.getCategory(), other.getContent(), ReportTimes.of(other.getCreatedAt())))
                        .toList());
    }

    /**
     * 대상 신고를 처리하고, 요청하면 같은 문제의 다른 미처리 신고에도 같은 상태와 답변을 적용한다.
     * 빈 답변은 null로 저장한다.
     */
    @Transactional
    public ReportUpdateResponse update(Long id, ReportUpdateRequest request) {
        Report report = findReport(id);
        List<Report> targets = new ArrayList<>();
        targets.add(report);
        if (request.applyToSameProblem()) {
            targets.addAll(sameProblemOpenReports(report));
        }
        String answer = request.answer() == null || request.answer().isBlank() ? null : request.answer().strip();
        LocalDateTime now = clockHolder.getCurrentDateTime();
        targets.forEach(target -> target.process(request.status(), answer, now));
        reportRepository.flush();
        return new ReportUpdateResponse(targets.stream().map(Report::getId).toList());
    }

    private List<Report> sameProblemOpenReports(Report report) {
        if (report.getProblem() == null) {
            return List.of();
        }
        return reportRepository.findAllByProblemIdAndStatusInAndIdNotOrderByCreatedAtDescIdDesc(
                report.getProblem().getId(), ReportStatus.OPEN, report.getId());
    }

    private Map<Long, Long> openCountsByProblem(List<Report> reports) {
        Set<Long> problemIds = reports.stream().map(Report::getProblem).filter(Objects::nonNull)
                .map(Problem::getId).collect(Collectors.toSet());
        if (problemIds.isEmpty()) {
            return Map.of();
        }
        return reportRepository.countByProblemIds(problemIds, ReportStatus.OPEN).stream()
                .collect(Collectors.toMap(ProblemCount::problemId, ProblemCount::count));
    }

    private long openCount(Map<Long, Long> openCounts, Report report) {
        return report.getProblem() == null ? 0 : openCounts.getOrDefault(report.getProblem().getId(), 0L);
    }

    private AdminReportSummaryResponse toSummary(Report report, long sameProblemOpenCount) {
        return new AdminReportSummaryResponse(report.getId(), report.getStatus(), report.getCategory(),
                new ReportQualificationExamResponse(report.getQualificationExam().getId(),
                        report.getQualificationExam().getName()),
                toProblem(report), report.getContent(), sameProblemOpenCount, ReportTimes.of(report.getCreatedAt()));
    }

    private AdminReportProblemResponse toProblem(Report report) {
        if (!report.hasProblemSnapshot()) {
            return null;
        }
        Problem problem = report.getProblem();
        return new AdminReportProblemResponse(problem == null ? null : problem.getId(), report.getProblemNumber(),
                problem == null ? null : problem.getExam().getId(), report.getExamName(), report.getExamYear(),
                problem == null ? null : problem.getSubjectMapping().getSubject().getId(), report.getSubjectName());
    }

    /** 앞 4자 + … + 뒤 3자. */
    static String mask(String deviceId) {
        if (deviceId == null || deviceId.length() < 8) {
            return "…";
        }
        return deviceId.substring(0, 4) + "…" + deviceId.substring(deviceId.length() - 3);
    }

    private Report findReport(Long id) {
        return reportRepository.findDetailById(id)
                .orElseThrow(() -> new ReportException(ExceptionMessage.REPORT_NOT_FOUND));
    }
}
