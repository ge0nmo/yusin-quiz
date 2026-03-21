package com.cpa.yusin.quiz.bookmark.controller.dto.response;

import java.util.List;

public record BookmarkStatusResponse(List<Long> bookmarkedIds) {

    public BookmarkStatusResponse {
        bookmarkedIds = List.copyOf(bookmarkedIds);
    }

    public static BookmarkStatusResponse of(List<Long> bookmarkedIds) {
        return new BookmarkStatusResponse(bookmarkedIds);
    }
}
