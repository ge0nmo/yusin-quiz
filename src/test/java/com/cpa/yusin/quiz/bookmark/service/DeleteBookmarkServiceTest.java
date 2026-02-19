package com.cpa.yusin.quiz.bookmark.service;

import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.global.exception.BookmarkException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteBookmarkServiceTest extends MockSetup {

    @Test
    @DisplayName("북마크 삭제 성공")
    void delete_success() {
        // given
        Long memberId = member1.getId();
        Long problemId = physicsProblem1.getId();
        testContainer.createBookmarkService.create(memberId, problemId);

        // when
        testContainer.deleteBookmarkService.delete(memberId, problemId);

        // then
        assertThat(testContainer.bookmarkRepository.existsByMemberIdAndProblemId(memberId, problemId))
                .isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 북마크를 삭제하면 예외 발생")
    void delete_notFound_throwsException() {
        // given
        Long memberId = member1.getId();
        Long problemId = physicsProblem1.getId();

        // when & then
        assertThatThrownBy(() -> testContainer.deleteBookmarkService.delete(memberId, problemId))
                .isInstanceOf(BookmarkException.class);
    }

    @Test
    @DisplayName("북마크 생성 후 삭제한 뒤 다시 생성 가능")
    void delete_thenRecreate_success() {
        // given
        Long memberId = member1.getId();
        Long problemId = physicsProblem1.getId();
        testContainer.createBookmarkService.create(memberId, problemId);
        testContainer.deleteBookmarkService.delete(memberId, problemId);

        // when (재생성)
        testContainer.createBookmarkService.create(memberId, problemId);

        // then
        assertThat(testContainer.bookmarkRepository.existsByMemberIdAndProblemId(memberId, problemId))
                .isTrue();
    }
}
