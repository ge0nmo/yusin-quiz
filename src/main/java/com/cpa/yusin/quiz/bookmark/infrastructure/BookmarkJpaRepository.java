package com.cpa.yusin.quiz.bookmark.infrastructure;

import com.cpa.yusin.quiz.bookmark.domain.Bookmark;
import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookmarkJpaRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByMemberIdAndProblemId(Long memberId, Long problemId);

    Optional<Bookmark> findByMemberIdAndProblemId(Long memberId, Long problemId);

    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.member.id = :memberId AND b.problem.id = :problemId")
    void deleteByMemberIdAndProblemId(@Param("memberId") Long memberId, @Param("problemId") Long problemId);

    /**
     * 전체 북마크 조회 (과목 필터 없음)
     * Problem → Exam 을 JOIN FETCH하여 N+1 방지
     * 북마크 row는 남아 있더라도, 삭제된 문제/시험/과목 아래 컨텐츠는 노출하지 않는다.
     */
    @Query("SELECT b FROM Bookmark b " +
            "JOIN FETCH b.problem p " +
            "JOIN FETCH p.exam e " +
            "WHERE b.member.id = :memberId " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false " +
            "   AND (s.status = :publishedStatus OR s.status IS NULL)" +
            ") " +
            "ORDER BY b.createdAt DESC")
    Slice<Bookmark> findAllByMemberId(@Param("memberId") Long memberId,
                                      @Param("publishedStatus") SubjectStatus publishedStatus,
                                      Pageable pageable);

    /**
     * 과목별 북마크 조회
     * Problem → Exam.subjectId 로 과목 필터링
     * subject filter 를 타더라도 soft-delete 된 상위 트리는 항상 제외한다.
     */
    @Query("SELECT b FROM Bookmark b " +
            "JOIN FETCH b.problem p " +
            "JOIN FETCH p.exam e " +
            "WHERE b.member.id = :memberId " +
            "AND e.subjectId = :subjectId " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false " +
            "   AND (s.status = :publishedStatus OR s.status IS NULL)" +
            ") " +
            "ORDER BY b.createdAt DESC")
    Slice<Bookmark> findAllByMemberIdAndSubjectId(
            @Param("memberId") Long memberId,
            @Param("subjectId") Long subjectId,
            @Param("publishedStatus") SubjectStatus publishedStatus,
            Pageable pageable);
}
