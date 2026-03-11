package com.cpa.yusin.quiz.problem.controller.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemLectureRequest {

    private String youtubeUrl;

    @Min(value = 0, message = "해설강의 시작 시간은 0 이상이어야 합니다.")
    private Integer startTimeSecond;
}
