package com.cpa.yusin.quiz.study.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.study.controller.dto.StudyLogResponse;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.StudyLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/study-logs")
@RestController
public class StudyLogController {

    private final StudyLogService studyLogService;

    @GetMapping("/monthly")
    public ResponseEntity<GlobalResponse<List<StudyLogResponse>>> getMonthlyLog(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam("yearMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

        List<DailyStudyLog> logs = studyLogService.getMonthlyLog(memberDetails.getMember().getId(), yearMonth);


        List<StudyLogResponse> response = logs.stream().map(StudyLogResponse::from).toList();
        return ResponseEntity.ok(GlobalResponse.success(response));
    }

    @GetMapping("/yearly")
    public ResponseEntity<GlobalResponse<List<StudyLogResponse>>> getYearlyLog(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam("year") int year) {

        List<DailyStudyLog> logs = studyLogService.getYearlyLog(memberDetails.getMember().getId(), year);
        List<StudyLogResponse> response = logs.stream().map(StudyLogResponse::from).toList();

        return ResponseEntity.ok(GlobalResponse.success(response));
    }

}
