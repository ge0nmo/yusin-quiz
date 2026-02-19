package com.cpa.yusin.quiz.study.controller.dto.request;

import com.cpa.yusin.quiz.study.domain.ExamMode;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamStartRequest {
    @NotNull(message = "시험 ID는 필수입니다.")
    private Long examId;

    @NotNull(message = "시험 모드는 필수입니다. (PRACTICE, REAL/EXAM)")
    private ExamMode mode;
}
