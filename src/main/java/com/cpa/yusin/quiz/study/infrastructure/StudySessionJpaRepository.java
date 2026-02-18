package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.ExamMode;
import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.StudySessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudySessionJpaRepository extends JpaRepository<StudySession, Long> {
    Optional<StudySession> findByMemberIdAndExamIdAndStatusAndMode(
            Long memberId, Long examId, StudySessionStatus status, ExamMode mode);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StudySession s where s.id = :id")
    Optional<StudySession> findByIdWithLock(@Param("id") Long id);
}
