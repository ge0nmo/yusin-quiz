package com.cpa.yusin.quiz.bookmark.controller.port;

public interface CreateBookmarkService {

    /**
     * 문제를 북마크에 추가합니다.
     *
     * @param memberId  북마크하는 회원 ID
     * @param problemId 북마크할 문제 ID
     */
    void create(Long memberId, Long problemId);
}
