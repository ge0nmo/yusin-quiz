package com.cpa.yusin.quiz.bookmark.service;

import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.global.exception.BookmarkException;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateBookmarkServiceTest extends MockSetup {

    @Test
    @DisplayName("북마크 생성 성공")
    void create_success() {
        // given
        Long memberId = member1.getId();
        Long problemId = physicsProblem1.getId();

        // when
        testContainer.createBookmarkService.create(memberId, problemId);

        // then
        assertThat(testContainer.bookmarkRepository.existsByMemberIdAndProblemId(memberId, problemId))
                .isTrue();
    }

    @Test
    @DisplayName("이미 북마크된 문제를 다시 북마크하면 예외 발생")
    void create_duplicateBookmark_throwsException() {
        // given
        Long memberId = member1.getId();
        Long problemId = physicsProblem1.getId();
        testContainer.createBookmarkService.create(memberId, problemId);

        // when & then
        assertThatThrownBy(() -> testContainer.createBookmarkService.create(memberId, problemId))
                .isInstanceOf(BookmarkException.class);
    }

    @Test
    @DisplayName("존재하지 않는 문제를 북마크하면 예외 발생")
    void create_problemNotFound_throwsException() {
        // given
        Long memberId = member1.getId();
        Long nonExistentProblemId = 999L;

        // when & then
        assertThatThrownBy(() -> testContainer.createBookmarkService.create(memberId, nonExistentProblemId))
                .isInstanceOf(ProblemException.class);
    }

    @Test
    @DisplayName("여러 문제를 각각 북마크 가능")
    void create_multipleBookmarks_success() {
        // given
        Long memberId = member1.getId();

        // when
        testContainer.createBookmarkService.create(memberId, physicsProblem1.getId());
        testContainer.createBookmarkService.create(memberId, physicsProblem2.getId());

        // then
        assertThat(testContainer.bookmarkRepository.existsByMemberIdAndProblemId(memberId, physicsProblem1.getId()))
                .isTrue();
        assertThat(testContainer.bookmarkRepository.existsByMemberIdAndProblemId(memberId, physicsProblem2.getId()))
                .isTrue();
    }
}
