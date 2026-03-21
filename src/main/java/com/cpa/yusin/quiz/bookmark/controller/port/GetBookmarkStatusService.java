package com.cpa.yusin.quiz.bookmark.controller.port;

import com.cpa.yusin.quiz.bookmark.controller.dto.request.BookmarkStatusRequest;
import com.cpa.yusin.quiz.bookmark.controller.dto.response.BookmarkStatusResponse;

public interface GetBookmarkStatusService {

    BookmarkStatusResponse getBookmarkStatus(Long memberId, BookmarkStatusRequest request);
}
