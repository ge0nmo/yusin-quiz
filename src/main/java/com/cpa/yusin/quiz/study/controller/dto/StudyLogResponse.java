package com.cpa.yusin.quiz.study.controller.dto;

import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StudyLogResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate date;

    private int count;

    public static StudyLogResponse from(DailyStudyLog log) {
        if (log == null)
            return null;

        return new StudyLogResponse(log.getDate(), log.getSolvedCount());
    }

}