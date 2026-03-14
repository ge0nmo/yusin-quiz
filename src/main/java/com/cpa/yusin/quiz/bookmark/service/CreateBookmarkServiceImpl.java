package com.cpa.yusin.quiz.bookmark.service;

import com.cpa.yusin.quiz.bookmark.controller.port.CreateBookmarkService;
import com.cpa.yusin.quiz.bookmark.domain.Bookmark;
import com.cpa.yusin.quiz.bookmark.service.port.BookmarkRepository;
import com.cpa.yusin.quiz.global.exception.BookmarkException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateBookmarkServiceImpl implements CreateBookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final ProblemRepository problemRepository;

    @Override
    public void create(Long memberId, Long problemId) {
        // 1. Member 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ExceptionMessage.USER_NOT_FOUND));

        // 2. Validate the active problem before duplicate check.
        // This prevents deleted problems from leaking as "already bookmarked".
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));

        // 3. 중복 북마크 체크
        if (bookmarkRepository.existsByMemberIdAndProblemId(memberId, problemId)) {
            throw new BookmarkException(ExceptionMessage.BOOKMARK_ALREADY_EXISTS);
        }

        // 4. 북마크 저장
        Bookmark bookmark = Bookmark.create(member, problem);
        bookmarkRepository.save(bookmark);
    }
}
