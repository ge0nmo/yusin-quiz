package com.cpa.yusin.quiz.report.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.report.controller.dto.PublicReportDto.*;
import com.cpa.yusin.quiz.report.service.PublicReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 앱의 문제 오류 신고와 건의·문의. 로그인이 없으므로 모든 요청은 {@code X-Device-Id} 헤더로 기기를 밝힌다.
 * {@link ReportDeviceId}를 첫 인자로 두어 기기 ID 오류가 본문 검증 오류보다 먼저 나가게 한다.
 */
@RestController
@RequestMapping("/api/v2/qualification-exams/{code}/reports")
@RequiredArgsConstructor
public class PublicReportV2Controller {
    private final PublicReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GlobalResponse<MyReportResponse> create(ReportDeviceId deviceId, @PathVariable String code,
                                                   @Valid @RequestBody ReportCreateRequest request) {
        return GlobalResponse.success(reportService.create(deviceId.value(), code, request));
    }

    @GetMapping
    public GlobalResponse<List<MyReportResponse>> getAll(ReportDeviceId deviceId, @PathVariable String code) {
        return GlobalResponse.success(reportService.getMyReports(deviceId.value(), code));
    }

    @GetMapping("/{id}")
    public GlobalResponse<MyReportResponse> get(ReportDeviceId deviceId, @PathVariable String code,
                                                @PathVariable Long id) {
        return GlobalResponse.success(reportService.getMyReport(deviceId.value(), code, id));
    }
}
