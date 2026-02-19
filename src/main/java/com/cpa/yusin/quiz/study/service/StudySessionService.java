package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.StudySessionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.domain.*;
import com.cpa.yusin.quiz.study.event.StudySolvedEvent;
import com.cpa.yusin.quiz.study.service.port.StudySessionRepository;
import com.cpa.yusin.quiz.study.service.port.SubmittedAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final SubmittedAnswerRepository submittedAnswerRepository;
    private final ChoiceRepository choiceRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ClockHolder clockHolder;

    /**
     * Start a study session.
     * If an IN_PROGRESS session exists for the same exam and mode, return it
     * (Resume).
     * Otherwise, create a new session.
     */
    @Transactional
    public StudySession startSession(Long memberId, Long examId, ExamMode mode) {
        Optional<StudySession> existingSession = studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(
                memberId, examId, StudySessionStatus.IN_PROGRESS, mode);

        if (existingSession.isPresent()) {
            return existingSession.get();
        }

        // Retrieve Member Reference for creation
        Member memberRef = memberRepository.getReferenceById(memberId);
        LocalDateTime now = clockHolder.getCurrentDateTime();
        StudySession newSession = StudySession.start(memberRef, examId, mode, now);
        return studySessionRepository.save(newSession);
    }

    public List<SubmittedAnswer> getSubmittedAnswers(Long sessionId) {
        return submittedAnswerRepository.findAllByStudySessionId(sessionId);
    }

    public StudySession getSession(Long sessionId) {
        return studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new StudySessionException(ExceptionMessage.SESSION_NOT_FOUND));
    }

    /**
     * Save a single answer.
     * Updates the last accessed index in the session.
     * Returns feedback (Explanation) if in PRACTICE mode.
     */
    @Transactional
    public ExamAnswerResponse saveAnswer(Long sessionId, Long problemId, Long choiceId, int index) {
        StudySession session = studySessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new StudySessionException(ExceptionMessage.SESSION_NOT_FOUND));

        // Update Session Index
        session.updateLastIndex(index);

        // Validate Choice and Get isCorrect
        Choice choice = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new StudySessionException(ExceptionMessage.CHOICE_NOT_FOUND));

        boolean isCorrect = choice.getIsAnswer();

        // Upsert Answer
        Optional<SubmittedAnswer> existingAnswer = submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId,
                problemId);

        if (existingAnswer.isPresent()) {
            existingAnswer.get().updateAnswer(choiceId, isCorrect);
        } else {
            SubmittedAnswer newAnswer = SubmittedAnswer.create(session, problemId, choiceId, isCorrect);
            submittedAnswerRepository.save(newAnswer);
        }

        // Record activity if in Practice Mode
        if (session.getMode() == ExamMode.PRACTICE) {
            eventPublisher.publishEvent(new StudySolvedEvent(session.getMember().getId(), 1));
            return ExamAnswerResponse.practice(isCorrect, getExplanationSafe(choice));
        }

        return ExamAnswerResponse.exam();
    }

    /**
     * Complete the session.
     * Calculates score server-side.
     * Records activity log (Batch update for Exam Mode).
     */
    @Transactional
    public int completeSession(Long sessionId) {
        StudySession session = studySessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new StudySessionException(ExceptionMessage.SESSION_NOT_FOUND));

        // Calculate Score
        List<SubmittedAnswer> answers = submittedAnswerRepository.findAllByStudySessionId(sessionId);
        int score = (int) answers.stream().filter(SubmittedAnswer::isCorrect).count();

        LocalDateTime now = clockHolder.getCurrentDateTime();
        session.complete(score, now);

        // Record Activity (Jandi)
        // Optimization: Batch Update for Exam Mode
        if (session.getMode() == ExamMode.EXAM) {
            int solvedCount = answers.size();
            eventPublisher.publishEvent(new StudySolvedEvent(session.getMember().getId(), solvedCount));
        }

        return score;
    }

    private String getExplanationSafe(Choice choice) {
        try {
            return choice.getProblem().getExplanation();
        } catch (Exception e) {
            log.warn("Failed to fetch explanation for choice {}", choice.getId(), e);
            return "해설을 불러올 수 없습니다.";
        }
    }
}
