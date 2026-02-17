package com.cpa.yusin.quiz.study.controller;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.study.controller.dto.StudyLogResponse;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.StudyLogService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/study-logs")
@RestController
public class StudyLogController {

    private final StudyLogService studyLogService;

    @GetMapping("/monthly")
    public List<StudyLogResponse> getMonthlyLog(
            @AuthenticationPrincipal Member member,
            @RequestParam("yearMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

        List<DailyStudyLog> logs = studyLogService.getMonthlyLog(member, yearMonth);
        return logs.stream().map(StudyLogResponse::from).toList();
    }

    @GetMapping("/yearly")
    public List<StudyLogResponse> getYearlyLog(
            @AuthenticationPrincipal Member member,
            @RequestParam("year") int year) {

        List<DailyStudyLog> logs = studyLogService.getYearlyLog(member, year);
        return logs.stream().map(StudyLogResponse::from).toList();
    }


}
