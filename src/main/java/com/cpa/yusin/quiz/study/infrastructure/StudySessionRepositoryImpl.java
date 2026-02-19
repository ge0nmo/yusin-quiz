package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.ExamMode;
import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.StudySessionStatus;
import com.cpa.yusin.quiz.study.service.port.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class StudySessionRepositoryImpl implements StudySessionRepository {

    private final StudySessionJpaRepository studySessionJpaRepository;

    @Override
    public StudySession save(StudySession studySession) {
        return studySessionJpaRepository.save(studySession);
    }

    @Override
    public Optional<StudySession> findById(Long id) {
        return studySessionJpaRepository.findById(id);
    }

    @Override
    public Optional<StudySession> findByIdWithLock(Long id) {
        return studySessionJpaRepository.findByIdWithLock(id);
    }

    @Override
    public Optional<StudySession> findByMemberIdAndExamIdAndStatusAndMode(Long memberId, Long examId,
            StudySessionStatus status, ExamMode mode) {
        return studySessionJpaRepository.findByMemberIdAndExamIdAndStatusAndMode(memberId, examId, status, mode);
    }
}
