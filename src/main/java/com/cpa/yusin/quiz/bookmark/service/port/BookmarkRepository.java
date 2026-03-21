package com.cpa.yusin.quiz.bookmark.service.port;

import com.cpa.yusin.quiz.bookmark.domain.Bookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository {

    Bookmark save(Bookmark bookmark);

    void deleteByMemberIdAndProblemId(Long memberId, Long problemId);

    boolean existsByMemberIdAndProblemId(Long memberId, Long problemId);

    Optional<Bookmark> findByMemberIdAndProblemId(Long memberId, Long problemId);

    List<Long> findBookmarkedProblemIds(Long memberId, Collection<Long> problemIds);

    /**
     * 특정 회원의 북마크된 문제를 Slice로 조회
     * 
     * @param subjectId null이면 전체 조회, 값이 있으면 과목별 필터
     */
    Slice<Bookmark> findByMemberIdAndSubjectId(Long memberId, Long subjectId, Pageable pageable);
}
