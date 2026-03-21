package com.cpa.yusin.quiz.bookmark.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkStatusRequest {

    @NotNull(message = "problemIds는 필수입니다")
    @Size(max = 500, message = "problemIds는 최대 500개까지 허용합니다")
    private List<
            @NotNull(message = "problemIds에는 null을 포함할 수 없습니다")
            @Positive(message = "problemIds는 양의 정수만 허용합니다")
            Long> problemIds;
}
