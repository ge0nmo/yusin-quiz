package com.cpa.yusin.quiz.bookmark.service;

import com.cpa.yusin.quiz.bookmark.controller.dto.request.BookmarkStatusRequest;
import com.cpa.yusin.quiz.bookmark.controller.dto.response.BookmarkStatusResponse;
import com.cpa.yusin.quiz.bookmark.controller.port.GetBookmarkStatusService;
import com.cpa.yusin.quiz.bookmark.service.port.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBookmarkStatusServiceImpl implements GetBookmarkStatusService {

    private final BookmarkRepository bookmarkRepository;

    @Override
    public BookmarkStatusResponse getBookmarkStatus(Long memberId, BookmarkStatusRequest request) {
        List<Long> problemIds = request.getProblemIds();
        if (problemIds == null || problemIds.isEmpty()) {
            return BookmarkStatusResponse.of(Collections.emptyList());
        }

        LinkedHashSet<Long> deduplicatedProblemIds = new LinkedHashSet<>(problemIds);
        Set<Long> bookmarkedIdSet = new HashSet<>(
                bookmarkRepository.findBookmarkedProblemIds(memberId, deduplicatedProblemIds));

        List<Long> bookmarkedIds = deduplicatedProblemIds.stream()
                .filter(bookmarkedIdSet::contains)
                .toList();

        return BookmarkStatusResponse.of(bookmarkedIds);
    }
}
