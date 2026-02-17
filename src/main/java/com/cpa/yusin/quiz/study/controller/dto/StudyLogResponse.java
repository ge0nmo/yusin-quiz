package com.cpa.yusin.quiz.study.controller.dto;

import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class StudyLogResponse
{
    private LocalDate date;

    private int count;

    public static StudyLogResponse from(DailyStudyLog log)
    {
        return new StudyLogResponse(log.getDate(), log.getSolvedCount());
    }

}