package com.cpa.yusin.quiz.question.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Data
public class QuestionDTO {
    private final long id;
    private final String title;
    private final String content;
    private final int answerCount;
    private final boolean answeredByAdmin;
    private final LocalDateTime createdAt;
    private final long problemId;

    // 회원 정보
    private final long memberId;
    private final String username;
}
