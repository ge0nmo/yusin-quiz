package com.cpa.yusin.quiz.bookmark.service;

import com.cpa.yusin.quiz.bookmark.controller.dto.BookmarkedProblemSliceResponse;
import com.cpa.yusin.quiz.bookmark.controller.port.GetBookmarkedProblemsService;
import com.cpa.yusin.quiz.bookmark.domain.Bookmark;
import com.cpa.yusin.quiz.bookmark.service.port.BookmarkRepository;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemLectureResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.ProblemContentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBookmarkedProblemsServiceImpl implements GetBookmarkedProblemsService {

        private final BookmarkRepository bookmarkRepository;
        private final ChoiceService choiceService;
        private final ProblemContentProcessor problemContentProcessor;

        @Override
        public BookmarkedProblemSliceResponse getBookmarkedProblems(
                        Long memberId, Long subjectId, int page, int size) {

                // 1. Slice 조회 (JOIN FETCH로 Problem + Exam 한 번에 로드)
                Slice<Bookmark> bookmarkSlice = bookmarkRepository.findByMemberIdAndSubjectId(
                                memberId, subjectId, PageRequest.of(page, size));

                List<Bookmark> bookmarks = bookmarkSlice.getContent();

                if (bookmarks.isEmpty()) {
                        return BookmarkedProblemSliceResponse.of(
                                        Collections.emptyList(), page, size, false);
                }

                // 2. 북마크된 문제들의 ID 추출
                List<Long> problemIds = bookmarks.stream()
                                .map(bookmark -> bookmark.getProblem().getId())
                                .toList();

                // 3. 보기(Choice)를 한 번에 조회하여 Map으로 변환 (N+1 방지)
                Map<Long, List<ChoiceResponse>> choicesMap = choiceService.findAllByProblemIds(problemIds);

                // 4. ProblemV2Response로 매핑
                List<ProblemV2Response> responses = bookmarks.stream()
                                .map(bookmark -> {
                                        Problem problem = bookmark.getProblem();
                                        List<ChoiceResponse> choices = choicesMap.getOrDefault(
                                                        problem.getId(), Collections.emptyList());

                                        return ProblemV2Response.builder()
                                                        .id(problem.getId())
                                                        .number(problem.getNumber())
                                                        .content(problemContentProcessor.processBlocksWithPresignedUrl(
                                                                        problem.getContentJson()))
                                                        .explanation(problemContentProcessor
                                                                        .processBlocksWithPresignedUrl(
                                                                                        problem.getExplanationJson()))
                                                        .lecture(ProblemLectureResponse.from(problem))
                                                        .choices(choices)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                return BookmarkedProblemSliceResponse.of(
                                responses, page, size, bookmarkSlice.hasNext());
        }
}
