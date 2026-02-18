package com.cpa.yusin.quiz.study.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmitRequest
{
    @NotNull(message = "세션 ID는 필수입니다.")
    private Long sessionId;

    @NotNull(message = "문제 ID는 필수입니다.")
    private Long problemId;

    @NotNull(message = "선택한 보기 ID는 필수입니다.")
    private Long choiceId;

    // 현재 몇번째 문제인지 (재접속 시 위치 복원용)
    private int index;
}
