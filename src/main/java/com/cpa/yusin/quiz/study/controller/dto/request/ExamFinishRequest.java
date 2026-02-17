package com.cpa.yusin.quiz.study.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamFinishRequest {
    @NotNull(message = "세션 ID는 필수입니다.")
    private Long sessionId;
}
