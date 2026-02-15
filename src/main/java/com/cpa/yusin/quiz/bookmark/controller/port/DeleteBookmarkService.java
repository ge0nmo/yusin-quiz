package com.cpa.yusin.quiz.bookmark.controller.port;

public interface DeleteBookmarkService {

    /**
     * 북마크를 삭제합니다.
     *
     * @param memberId  삭제 요청한 회원 ID
     * @param problemId 삭제할 문제 ID
     */
    void delete(Long memberId, Long problemId);
}
