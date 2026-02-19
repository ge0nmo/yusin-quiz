package com.cpa.yusin.quiz.bookmark.controller.port;

import com.cpa.yusin.quiz.bookmark.controller.dto.BookmarkedProblemSliceResponse;

public interface GetBookmarkedProblemsService {

    /**
     * 북마크된 문제를 Slice 방식으로 조회합니다.
     *
     * @param memberId  회원 ID
     * @param subjectId 과목 ID (null이면 전체 조회)
     * @param page      현재 페이지 번호 (0-based)
     * @param size      한 페이지 크기
     * @return 북마크된 문제 Slice 응답 (ProblemV2Response 포함)
     */
    BookmarkedProblemSliceResponse getBookmarkedProblems(Long memberId, Long subjectId, int page, int size);
}
