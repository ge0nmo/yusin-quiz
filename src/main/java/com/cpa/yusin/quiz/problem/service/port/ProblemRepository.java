package com.cpa.yusin.quiz.problem.service.port;

import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchCondition;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchProjection;
import com.cpa.yusin.quiz.problem.service.dto.WordProblemCandidateProjection;
import com.cpa.yusin.quiz.problem.service.dto.WordProblemCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProblemRepository
{
    Problem save(Problem problem);

    List<Problem> findAll();

    List<Problem> findAllByExamId(long examId);

    Optional<Problem> findById(long id);

    boolean existsByExamIdAndNumber(Long examId, int number);

    Optional<Problem> findRemovedByExamIdAndNumber(long examId, int number);

    Integer findMinimumNumberByExamId(long examId);

    long countActiveByExamId(long examId);

    Map<Long, Long> countActiveByExamIds(List<Long> examIds);

    List<WordProblemCountProjection> countPublishedWordProblemsBySubject();

    List<WordProblemCandidateProjection> findPublishedWordProblemCandidatesBySubjectId(Long subjectId);

    /** 회차에 저장된 ID들을 공개 상태인 말문제로 한 번에 다시 읽는다. */
    List<Problem> findPublishedWordProblemsByIds(List<Long> problemIds);

    void flush();

    Page<AdminProblemSearchProjection> searchAdminProblems(Pageable pageable, AdminProblemSearchCondition searchCondition);
}
