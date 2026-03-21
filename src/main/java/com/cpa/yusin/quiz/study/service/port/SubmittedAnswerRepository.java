package com.cpa.yusin.quiz.study.service.port;

import com.cpa.yusin.quiz.study.domain.SubmittedAnswer;
import com.cpa.yusin.quiz.study.service.dto.SubmittedAnswerCorrectnessSnapshot;

import java.util.List;
import java.util.Optional;

public interface SubmittedAnswerRepository {
    SubmittedAnswer save(SubmittedAnswer submittedAnswer);

    Optional<SubmittedAnswer> findByStudySessionIdAndProblemId(Long studySessionId, Long problemId);

    List<SubmittedAnswer> findAllByStudySessionId(Long studySessionId);

    List<SubmittedAnswerCorrectnessSnapshot> findCorrectnessSnapshotsByStudySessionId(Long studySessionId);
}
