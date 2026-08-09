package com.cpa.yusin.quiz.answer.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Builder
public class AnswerDTO {
    private final long id;
    private final String content;
    private final LocalDateTime createdAt;

    // 회원 정보
    private final long memberId;
    private final String username;

    @JsonProperty("isAdmin")
    private final boolean admin;
}
