package com.cpa.yusin.quiz.study.controller.dto.request;

import com.cpa.yusin.quiz.study.domain.ExamMode;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamStartRequest {
    @NotNull(message = "시험 ID는 필수입니다.")
    private Long examId;

    @NotNull(message = "시험 모드는 필수입니다.")
    private ExamMode mode;
}
