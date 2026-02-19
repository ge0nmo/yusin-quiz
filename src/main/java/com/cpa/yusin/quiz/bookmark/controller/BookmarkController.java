package com.cpa.yusin.quiz.bookmark.controller;

import com.cpa.yusin.quiz.bookmark.controller.dto.BookmarkedProblemSliceResponse;
import com.cpa.yusin.quiz.bookmark.controller.port.CreateBookmarkService;
import com.cpa.yusin.quiz.bookmark.controller.port.DeleteBookmarkService;
import com.cpa.yusin.quiz.bookmark.controller.port.GetBookmarkedProblemsService;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/bookmarks")
@RestController
public class BookmarkController {

    private final CreateBookmarkService createBookmarkService;
    private final DeleteBookmarkService deleteBookmarkService;
    private final GetBookmarkedProblemsService getBookmarkedProblemsService;

    /**
     * 북마크 추가
     *
     * @param problemId     북마크할 문제 ID
     * @param memberDetails 인증된 사용자 정보
     */
    @PostMapping("/{problemId}")
    public ResponseEntity<Void> create(
            @PathVariable Long problemId,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        createBookmarkService.create(memberDetails.getMember().getId(), problemId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 북마크 삭제
     *
     * @param problemId     삭제할 문제 ID
     * @param memberDetails 인증된 사용자 정보
     */
    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long problemId,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        deleteBookmarkService.delete(memberDetails.getMember().getId(), problemId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 북마크된 문제 Slice 조회
     *
     * @param subjectId     과목 ID (optional, 없으면 전체 조회)
     * @param page          페이지 번호 (0-based, default: 0)
     * @param size          페이지 크기 (default: 20)
     * @param memberDetails 인증된 사용자 정보
     */
    @GetMapping("/problems")
    public ResponseEntity<GlobalResponse<BookmarkedProblemSliceResponse>> getBookmarkedProblems(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal MemberDetails memberDetails) {

        BookmarkedProblemSliceResponse response = getBookmarkedProblemsService
                .getBookmarkedProblems(memberDetails.getMember().getId(), subjectId, page, size);

        return ResponseEntity.ok(GlobalResponse.success(response));
    }
}
