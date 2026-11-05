package com.cpa.yusin.quiz.report.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.report.controller.dto.AdminReportDto.*;
import com.cpa.yusin.quiz.report.domain.ReportCategoryGroup;
import com.cpa.yusin.quiz.report.domain.ReportSort;
import com.cpa.yusin.quiz.report.domain.ReportStatusGroup;
import com.cpa.yusin.quiz.report.service.AdminReportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Validated
public class AdminReportController {
    private final AdminReportService reportService;

    /** {@code page}는 1부터, {@code size}는 최대 100이다. */
    @GetMapping
    public GlobalResponse<List<AdminReportSummaryResponse>> getAll(
            @RequestParam(defaultValue = "OPEN") ReportStatusGroup statusGroup,
            @RequestParam(required = false) Long qualificationExamId,
            @RequestParam(required = false) ReportCategoryGroup categoryGroup,
            @RequestParam(defaultValue = "SAME_PROBLEM") ReportSort sort,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Page<AdminReportSummaryResponse> reports = reportService.getReports(statusGroup, qualificationExamId,
                categoryGroup, sort, page, size);
        return new GlobalResponse<>(reports.getContent(), PageInfo.of(reports));
    }

    @GetMapping("/summary")
    public GlobalResponse<ReportCountResponse> getSummary(@RequestParam(required = false) Long qualificationExamId) {
        return GlobalResponse.success(reportService.getCounts(qualificationExamId));
    }

    @GetMapping("/{id}")
    public GlobalResponse<AdminReportDetailResponse> get(@PathVariable Long id) {
        return GlobalResponse.success(reportService.getReport(id));
    }

    @PutMapping("/{id}")
    public GlobalResponse<ReportUpdateResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody ReportUpdateRequest request) {
        return GlobalResponse.success(reportService.update(id, request));
    }
}
