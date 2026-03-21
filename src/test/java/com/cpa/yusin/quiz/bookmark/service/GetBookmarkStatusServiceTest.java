package com.cpa.yusin.quiz.bookmark.service;

import com.cpa.yusin.quiz.bookmark.controller.dto.request.BookmarkStatusRequest;
import com.cpa.yusin.quiz.bookmark.controller.dto.response.BookmarkStatusResponse;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetBookmarkStatusServiceTest extends MockSetup {

    @Test
    @DisplayName("북마크 상태 조회는 중복을 제거하고 입력 순서를 보존한다")
    void getBookmarkStatus_dedupesAndPreservesOrder() {
        Long memberId = member1.getId();
        testContainer.createBookmarkService.create(memberId, physicsProblem1.getId());
        testContainer.createBookmarkService.create(memberId, physicsProblem2.getId());

        BookmarkStatusRequest request = BookmarkStatusRequest.builder()
                .problemIds(List.of(physicsProblem2.getId(), 999L, physicsProblem2.getId(), physicsProblem1.getId()))
                .build();

        BookmarkStatusResponse response = testContainer.getBookmarkStatusService.getBookmarkStatus(memberId, request);

        assertThat(response.bookmarkedIds()).containsExactly(physicsProblem2.getId(), physicsProblem1.getId());
    }

    @Test
    @DisplayName("북마크 상태 조회는 다른 회원의 북마크와 존재하지 않는 문제를 무시한다")
    void getBookmarkStatus_ignoresIdsOutsideMemberIntersection() {
        Long memberId = member1.getId();
        Member otherMember = testContainer.memberRepository.save(Member.builder()
                .id(2L)
                .email("other@test.com")
                .password("password")
                .username("other-user")
                .platform(Platform.HOME)
                .role(Role.USER)
                .build());

        testContainer.createBookmarkService.create(otherMember.getId(), physicsProblem1.getId());
        testContainer.createBookmarkService.create(memberId, physicsProblem2.getId());

        BookmarkStatusRequest request = BookmarkStatusRequest.builder()
                .problemIds(List.of(physicsProblem1.getId(), 12345L, physicsProblem2.getId()))
                .build();

        BookmarkStatusResponse response = testContainer.getBookmarkStatusService.getBookmarkStatus(memberId, request);

        assertThat(response.bookmarkedIds()).containsExactly(physicsProblem2.getId());
    }

    @Test
    @DisplayName("북마크 상태 조회는 빈 배열 요청에 빈 배열로 응답한다")
    void getBookmarkStatus_emptyRequestReturnsEmptyResponse() {
        BookmarkStatusRequest request = BookmarkStatusRequest.builder()
                .problemIds(List.of())
                .build();

        BookmarkStatusResponse response = testContainer.getBookmarkStatusService
                .getBookmarkStatus(member1.getId(), request);

        assertThat(response.bookmarkedIds()).isEmpty();
    }
}
