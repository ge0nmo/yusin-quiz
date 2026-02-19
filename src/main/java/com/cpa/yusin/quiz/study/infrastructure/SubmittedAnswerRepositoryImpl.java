package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.SubmittedAnswer;
import com.cpa.yusin.quiz.study.service.port.SubmittedAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class SubmittedAnswerRepositoryImpl implements SubmittedAnswerRepository {

    private final SubmittedAnswerJpaRepository submittedAnswerJpaRepository;

    @Override
    public SubmittedAnswer save(SubmittedAnswer submittedAnswer) {
        return submittedAnswerJpaRepository.save(submittedAnswer);
    }

    @Override
    public Optional<SubmittedAnswer> findByStudySessionIdAndProblemId(Long studySessionId, Long problemId) {
        return submittedAnswerJpaRepository.findByStudySessionIdAndProblemId(studySessionId, problemId);
    }

    @Override
    public List<SubmittedAnswer> findAllByStudySessionId(Long studySessionId) {
        return submittedAnswerJpaRepository.findAllByStudySessionId(studySessionId);
    }
}
