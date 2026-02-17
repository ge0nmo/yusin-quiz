package com.cpa.yusin.quiz.study.service.port;

import com.cpa.yusin.quiz.study.domain.ExamMode;
import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.StudySessionStatus;

import java.util.Optional;

public interface StudySessionRepository {
    StudySession save(StudySession studySession);

    Optional<StudySession> findById(Long id);

    Optional<StudySession> findByMemberIdAndExamIdAndStatusAndMode(
            Long memberId, Long examId, StudySessionStatus status, ExamMode mode);
}
