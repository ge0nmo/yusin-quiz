package com.cpa.yusin.quiz.bookmark.service;

import com.cpa.yusin.quiz.bookmark.controller.dto.BookmarkedProblemSliceResponse;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.domain.Problem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetBookmarkedProblemsServiceTest extends MockSetup {

        @Test
        @DisplayName("북마크된 문제 전체 조회 성공")
        void getBookmarkedProblems_all_success() {
                // given
                Long memberId = member1.getId();
                testContainer.createBookmarkService.create(memberId, physicsProblem1.getId());
                testContainer.createBookmarkService.create(memberId, physicsProblem2.getId());

                // when
                BookmarkedProblemSliceResponse response = testContainer.getBookmarkedProblemsService
                                .getBookmarkedProblems(memberId, null, 0, 20);

                // then
                assertThat(response.getContent()).hasSize(2);
                assertThat(response.isHasNext()).isFalse();
                assertThat(response.getCurrentPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("북마크된 문제 과목별 조회 - Physics만")
        void getBookmarkedProblems_bySubject_physics() {
                // given
                Long memberId = member1.getId();
                // Physics 문제 2개 + Biology 문제 1개 북마크
                testContainer.createBookmarkService.create(memberId, physicsProblem1.getId());
                testContainer.createBookmarkService.create(memberId, physicsProblem2.getId());

                Problem biologyProblem = testContainer.problemRepository.save(Problem.builder()
                                .id(10L)
                                .content("biology content")
                                .number(1)
                                .exam(biologyExam1)
                                .build());
                testContainer.createBookmarkService.create(memberId, biologyProblem.getId());

                // when - Physics(subjectId=1) 필터
                BookmarkedProblemSliceResponse response = testContainer.getBookmarkedProblemsService
                                .getBookmarkedProblems(
                                                memberId, physics.getId(), 0, 20);

                // then
                assertThat(response.getContent()).hasSize(2);
                assertThat(response.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("북마크된 문제 과목별 조회 - Biology만")
        void getBookmarkedProblems_bySubject_biology() {
                // given
                Long memberId = member1.getId();
                testContainer.createBookmarkService.create(memberId, physicsProblem1.getId());

                Problem biologyProblem = testContainer.problemRepository.save(Problem.builder()
                                .id(10L)
                                .content("biology content")
                                .number(1)
                                .exam(biologyExam1)
                                .build());
                testContainer.createBookmarkService.create(memberId, biologyProblem.getId());

                // when - Biology(subjectId=2) 필터
                BookmarkedProblemSliceResponse response = testContainer.getBookmarkedProblemsService
                                .getBookmarkedProblems(
                                                memberId, biology.getId(), 0, 20);

                // then
                assertThat(response.getContent()).hasSize(1);
                assertThat(response.getContent().get(0).getId()).isEqualTo(biologyProblem.getId());
        }

        @Test
        @DisplayName("hasNext 판단 - 다음 페이지가 있는 경우")
        void getBookmarkedProblems_hasNext_true() {
                // given
                Long memberId = member1.getId();
                testContainer.createBookmarkService.create(memberId, physicsProblem1.getId());
                testContainer.createBookmarkService.create(memberId, physicsProblem2.getId());

                Problem extraProblem = testContainer.problemRepository.save(Problem.builder()
                                .id(10L)
                                .content("extra content")
                                .number(3)
                                .exam(physicsExam1)
                                .build());
                testContainer.createBookmarkService.create(memberId, extraProblem.getId());

                // when - size=2로 조회 → 3개 중 2개만 가져오므로 hasNext=true
                BookmarkedProblemSliceResponse response = testContainer.getBookmarkedProblemsService
                                .getBookmarkedProblems(memberId, null, 0, 2);

                // then
                assertThat(response.getContent()).hasSize(2);
                assertThat(response.isHasNext()).isTrue();
        }

        @Test
        @DisplayName("북마크가 없는 경우 빈 결과 반환")
        void getBookmarkedProblems_empty() {
                // given
                Long memberId = member1.getId();

                // when
                BookmarkedProblemSliceResponse response = testContainer.getBookmarkedProblemsService
                                .getBookmarkedProblems(memberId, null, 0, 20);

                // then
                assertThat(response.getContent()).isEmpty();
                assertThat(response.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("북마크된 문제에 보기(Choice)가 포함되어 반환")
        void getBookmarkedProblems_withChoices() {
                // given
                Long memberId = member1.getId();
                testContainer.createBookmarkService.create(memberId, physicsProblem1.getId());

                // when
                BookmarkedProblemSliceResponse response = testContainer.getBookmarkedProblemsService
                                .getBookmarkedProblems(memberId, null, 0, 20);

                // then
                assertThat(response.getContent()).hasSize(1);
                assertThat(response.getContent().get(0).getChoices()).hasSize(3);
                assertThat(response.getContent().get(0).getChoices().get(0).content()).isEqualTo("choice 1");
        }
}
