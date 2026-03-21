package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.SubmittedAnswer;
import com.cpa.yusin.quiz.study.service.dto.SubmittedAnswerCorrectnessSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmittedAnswerJpaRepository extends JpaRepository<SubmittedAnswer, Long> {
    Optional<SubmittedAnswer> findByStudySessionIdAndProblemId(Long studySessionId, Long problemId);

    List<SubmittedAnswer> findAllByStudySessionId(Long studySessionId);

    @Query("SELECT new com.cpa.yusin.quiz.study.service.dto.SubmittedAnswerCorrectnessSnapshot(" +
            "sa.problemId, sa.choiceId, c.isAnswer) " +
            "FROM SubmittedAnswer sa " +
            "LEFT JOIN Choice c ON c.id = sa.choiceId " +
            "WHERE sa.studySession.id = :studySessionId")
    List<SubmittedAnswerCorrectnessSnapshot> findCorrectnessSnapshotsByStudySessionId(
            @Param("studySessionId") Long studySessionId
    );
}
