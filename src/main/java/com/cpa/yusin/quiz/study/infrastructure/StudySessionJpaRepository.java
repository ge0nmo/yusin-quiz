package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.ExamMode;
import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.StudySessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudySessionJpaRepository extends JpaRepository<StudySession, Long> {
    Optional<StudySession> findByMemberIdAndExamIdAndStatusAndMode(
            Long memberId, Long examId, StudySessionStatus status, ExamMode mode);
}
