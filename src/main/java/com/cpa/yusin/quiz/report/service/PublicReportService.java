package com.cpa.yusin.quiz.report.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ReportException;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.infrastructure.ProblemRepository;
import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamRepository;
import com.cpa.yusin.quiz.report.controller.dto.PublicReportDto.*;
import com.cpa.yusin.quiz.report.domain.Report;
import com.cpa.yusin.quiz.report.domain.ReportStatus;
import com.cpa.yusin.quiz.report.infrastructure.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * 앱의 문제 오류 신고와 건의·문의. 회원이 없으므로 기기 ID와 자격시험 코드 범위 안에서만 읽고 쓴다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicReportService {
    static final int DAILY_LIMIT = 20;
    static final Duration RATE_LIMIT_WINDOW = Duration.ofHours(24);

    private final ReportRepository reportRepository;
    private final QualificationExamRepository qualificationExamRepository;
    private final ProblemRepository problemRepository;
    private final ClockHolder clockHolder;

    @Transactional
    public MyReportResponse create(String deviceId, String code, ReportCreateRequest request) {
        QualificationExam qualificationExam = findQualification(code);
        Problem problem = request.problemId() == null ? null : findProblem(qualificationExam, request.problemId());
        if (problem != null && reportRepository.existsByDeviceIdAndProblemIdAndCategoryAndStatusIn(
                deviceId, problem.getId(), request.category(), ReportStatus.OPEN)) {
            throw new ReportException(ExceptionMessage.REPORT_DUPLICATED);
        }
        if (reportRepository.countByDeviceIdAndCreatedAtAfter(deviceId,
                clockHolder.getCurrentDateTime().minus(RATE_LIMIT_WINDOW)) >= DAILY_LIMIT) {
            throw new ReportException(ExceptionMessage.REPORT_RATE_LIMITED);
        }
        Report report = reportRepository.saveAndFlush(new Report(qualificationExam, problem, request.category(),
                request.content().strip(), request.selectedChoiceNumbers().stream().sorted().toList(),
                deviceId, request.appVersion().strip(), request.platform()));
        return toResponse(report);
    }

    /** 최신순 최대 100건. */
    public List<MyReportResponse> getMyReports(String deviceId, String code) {
        QualificationExam qualificationExam = findQualification(code);
        return reportRepository.findTop100ByDeviceIdAndQualificationExamIdOrderByCreatedAtDescIdDesc(
                        deviceId, qualificationExam.getId()).stream()
                .map(PublicReportService::toResponse)
                .toList();
    }

    public MyReportResponse getMyReport(String deviceId, String code, Long id) {
        QualificationExam qualificationExam = findQualification(code);
        return reportRepository.findByIdAndDeviceIdAndQualificationExamId(id, deviceId, qualificationExam.getId())
                .map(PublicReportService::toResponse)
                .orElseThrow(() -> new ReportException(ExceptionMessage.REPORT_NOT_FOUND));
    }

    /** 앱에는 종료 상태의 답변만 보인다. 미처리로 되돌린 신고의 저장된 답변은 숨긴다. */
    static MyReportResponse toResponse(Report report) {
        boolean answered = report.getStatus().isClosed();
        return new MyReportResponse(report.getId(), report.getCategory(), report.getStatus(), toProblem(report),
                report.getContent(), report.getSelectedChoiceNumbers(),
                answered ? report.getAnswer() : null,
                answered ? ReportTimes.of(report.getAnsweredAt()) : null,
                ReportTimes.of(report.getCreatedAt()), ReportTimes.of(report.getUpdatedAt()));
    }

    private static ReportProblemResponse toProblem(Report report) {
        if (!report.hasProblemSnapshot()) {
            return null;
        }
        Long problemId = report.getProblem() == null ? null : report.getProblem().getId();
        return new ReportProblemResponse(problemId, report.getProblemNumber(), report.getExamName(),
                report.getExamYear(), report.getSubjectName());
    }

    /** 신고는 공개 여부와 관계없이 등록된 자격시험이면 받는다. 모르는 코드만 404다. */
    private QualificationExam findQualification(String code) {
        QualificationExamCode qualificationCode = QualificationExamCode.from(code)
                .orElseThrow(() -> new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND));
        return qualificationExamRepository.findByCode(qualificationCode)
                .orElseThrow(() -> new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND));
    }

    private Problem findProblem(QualificationExam qualificationExam, Long problemId) {
        return problemRepository.findDetailById(problemId)
                .filter(problem -> Objects.equals(
                        problem.getExam().getQualificationExam().getId(), qualificationExam.getId()))
                .orElseThrow(() -> new ContentException(ExceptionMessage.PROBLEM_NOT_FOUND));
    }
}
