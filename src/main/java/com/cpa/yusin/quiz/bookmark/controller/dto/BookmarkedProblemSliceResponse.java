package com.cpa.yusin.quiz.bookmark.controller.dto;

import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 북마크된 문제 Slice 조회 응답 DTO
 *
 * <p>
 * Slice 기반 페이지네이션: count 쿼리 없이 hasNext로 다음 페이지 존재 여부 판단
 * </p>
 *
 * <pre>
 * {
 *   "content": [ { ProblemV2Response... }, ... ],
 *   "currentPage": 0,
 *   "size": 20,
 *   "hasNext": true
 * }
 * </pre>
 */
@Data
@Builder
public class BookmarkedProblemSliceResponse {

    private List<ProblemV2Response> content;

    private int currentPage;

    private int size;

    private boolean hasNext;

    public static BookmarkedProblemSliceResponse of(
            List<ProblemV2Response> content, int currentPage, int size, boolean hasNext) {
        return BookmarkedProblemSliceResponse.builder()
                .content(content)
                .currentPage(currentPage)
                .size(size)
                .hasNext(hasNext)
                .build();
    }
}
