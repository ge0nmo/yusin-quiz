package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.global.exception.StudySessionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.InProgressSessionResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.StudyProgressAbandonResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.StudySummaryResponse;
import com.cpa.yusin.quiz.study.domain.*;
import com.cpa.yusin.quiz.study.event.StudySolvedEvent;
import com.cpa.yusin.quiz.study.service.port.StudySessionRepository;
import com.cpa.yusin.quiz.study.service.port.SubmittedAnswerRepository;
import com.cpa.yusin.quiz.study.service.dto.StudySessionCompletionSummary;
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

    private static final int MAX_DEVICE_ID_LENGTH = 64;

    private final StudySessionRepository studySessionRepository;
    private final SubmittedAnswerRepository submittedAnswerRepository;
    private final ChoiceRepository choiceRepository;
    private final MemberRepository memberRepository;
    private final ExamService examService;
    private final ProblemService problemService;
    private final ProblemRepository problemRepository;
    private final StudyLogService studyLogService;
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
        return startSession(memberId, examId, mode, null);
    }

    @Transactional
    public StudySession startSession(Long memberId, Long examId, ExamMode mode, String deviceId) {
        String normalizedDeviceId = normalizeDeviceId(deviceId);
        Member lockedMember = memberRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new MemberException(ExceptionMessage.USER_NOT_FOUND));

        examService.findPublishedById(examId);

        Optional<StudySession> existingSession = studySessionRepository.findByMemberIdAndExamIdAndStatusAndModeWithLock(
                memberId, examId, StudySessionStatus.IN_PROGRESS, mode);

        if (existingSession.isPresent()) {
            StudySession resumedSession = existingSession.get();
            resumedSession.claimDevice(normalizedDeviceId);
            backfillPlannedProblemCountIfMissing(resumedSession, examId, countAnsweredProblems(resumedSession.getId()));
            return resumedSession;
        }

        LocalDateTime now = clockHolder.getCurrentDateTime();
        int plannedProblemCount = Math.toIntExact(problemRepository.countActiveByExamId(examId));
        StudySession newSession = StudySession.start(
                lockedMember,
                examId,
                mode,
                now,
                plannedProblemCount,
                normalizedDeviceId
        );
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
    public ExamAnswerResponse saveAnswer(Long memberId, Long sessionId, Long problemId, Long choiceId, int index) {
        return saveAnswer(memberId, sessionId, problemId, choiceId, index, null);
    }

    @Transactional
    public ExamAnswerResponse saveAnswer(
            Long memberId,
            Long sessionId,
            Long problemId,
            Long choiceId,
            int index,
            String deviceId
    ) {
        String normalizedDeviceId = normalizeDeviceId(deviceId);
        StudySession session = studySessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new StudySessionException(ExceptionMessage.SESSION_NOT_FOUND));
        validateOwnership(session, memberId);
        validateInProgress(session);
        validateAndClaimDevice(session, normalizedDeviceId);

        examService.findPublishedById(session.getExamId());

        session.updateLastIndex(index);

        Problem problem = problemService.findById(problemId);
        validateProblemBelongsToSession(problem, session.getExamId());

        Choice choice = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new StudySessionException(ExceptionMessage.CHOICE_NOT_FOUND));
        validateChoiceBelongsToProblem(choice, problemId);

        boolean isCorrect = choice.getIsAnswer();

        Optional<SubmittedAnswer> existingAnswer = submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId,
                problemId);

        if (existingAnswer.isPresent()) {
            existingAnswer.get().updateAnswer(choiceId, isCorrect);
        } else {
            SubmittedAnswer newAnswer = SubmittedAnswer.create(session, problemId, choiceId, isCorrect);
            submittedAnswerRepository.save(newAnswer);
        }

        if (session.getMode() == ExamMode.PRACTICE) {
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
    public StudySessionCompletionSummary completeSession(Long memberId, Long sessionId) {
        return completeSession(memberId, sessionId, null);
    }

    @Transactional
    public StudySessionCompletionSummary completeSession(Long memberId, Long sessionId, String deviceId) {
        String normalizedDeviceId = normalizeDeviceId(deviceId);
        StudySession session = studySessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new StudySessionException(ExceptionMessage.SESSION_NOT_FOUND));
        validateOwnership(session, memberId);

        if (!session.isInProgress() && session.getStatus() != StudySessionStatus.COMPLETED) {
            throw new StudySessionException(ExceptionMessage.SESSION_NOT_IN_PROGRESS);
        }

        validateAndClaimDevice(session, normalizedDeviceId);

        List<SubmittedAnswer> answers = submittedAnswerRepository.findAllByStudySessionId(sessionId);
        backfillPlannedProblemCountIfMissing(session, session.getExamId(), answers.size());
        StudySessionCompletionSummary summary = buildCompletionSummary(session, answers);

        if (!session.isInProgress()) {
            return summary;
        }

        LocalDateTime now = clockHolder.getCurrentDateTime();
        session.complete(summary.correctCount(), now);

        if (shouldRecordCompletedLearningUnit(session, summary)) {
            eventPublisher.publishEvent(new StudySolvedEvent(session.getMember().getId(), summary.answeredCount()));
        }

        return summary;
    }

    public StudySummaryResponse getStudySummary(Long memberId) {
        int todaySolved = studyLogService.getTodaySolved(memberId);
        int currentStreak = studyLogService.calculateCurrentStreak(memberId);
        int yearSolved = studyLogService.getYearSolvedCount(memberId, clockHolder.getCurrentDateTime().getYear());

        List<InProgressSessionResponse> inProgressSessions = studySessionRepository
                .findAllByMemberIdAndStatusOrderByUpdatedAtDesc(memberId, StudySessionStatus.IN_PROGRESS)
                .stream()
                .map(this::toInProgressSessionResponse)
                .toList();
        InProgressSessionResponse inProgress = inProgressSessions.isEmpty() ? null : inProgressSessions.get(0);

        return new StudySummaryResponse(
                todaySolved,
                currentStreak,
                yearSolved,
                inProgress,
                inProgressSessions
        );
    }

    @Transactional
    public StudyProgressAbandonResponse abandonProgress(Long memberId, Long examId, ExamMode mode) {
        List<StudySession> sessions;
        if (examId == null) {
            sessions = studySessionRepository.findByMemberIdAndStatus(memberId, StudySessionStatus.IN_PROGRESS);
        } else if (mode == null) {
            sessions = studySessionRepository.findByMemberIdAndExamIdAndStatus(memberId, examId, StudySessionStatus.IN_PROGRESS);
        } else {
            sessions = studySessionRepository.findAllByMemberIdAndExamIdAndStatusAndMode(memberId, examId, StudySessionStatus.IN_PROGRESS, mode);
        }

        for (StudySession session : sessions) {
            session.abandon();
            studySessionRepository.save(session);
        }

        return new StudyProgressAbandonResponse(sessions.size());
    }

    private void validateOwnership(StudySession session, Long memberId) {
        if (!session.isOwnedBy(memberId)) {
            throw new MemberException(ExceptionMessage.NO_AUTHORIZATION);
        }
    }

    private void validateInProgress(StudySession session) {
        if (!session.isInProgress()) {
            throw new StudySessionException(ExceptionMessage.SESSION_NOT_IN_PROGRESS);
        }
    }

    private void validateAndClaimDevice(StudySession session, String deviceId) {
        if (!session.isOwnedByDevice(deviceId)) {
            throw new StudySessionException(ExceptionMessage.SESSION_TAKEN_OVER);
        }

        session.claimDevice(deviceId);
    }

    private String normalizeDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }

        String normalized = deviceId.trim();
        if (normalized.length() > MAX_DEVICE_ID_LENGTH) {
            throw new StudySessionException(ExceptionMessage.INVALID_DATA);
        }

        return normalized;
    }

    private void backfillPlannedProblemCountIfMissing(StudySession session, Long examId, int answeredCount) {
        if (session.getPlannedProblemCount() != null) {
            return;
        }

        int currentActiveProblemCount = Math.toIntExact(problemRepository.countActiveByExamId(examId));
        session.assignPlannedProblemCount(Math.max(currentActiveProblemCount, answeredCount));
    }

    private int countAnsweredProblems(Long sessionId) {
        return submittedAnswerRepository.findAllByStudySessionId(sessionId).size();
    }

    private StudySessionCompletionSummary buildCompletionSummary(StudySession session, List<SubmittedAnswer> answers) {
        int answeredCount = answers.size();
        int totalCount = session.getPlannedProblemCount() == null
                ? answeredCount
                : Math.max(session.getPlannedProblemCount(), answeredCount);
        int correctCount = calculateCorrectCount(answers);
        int unansweredCount = Math.max(totalCount - answeredCount, 0);

        return new StudySessionCompletionSummary(correctCount, totalCount, answeredCount, unansweredCount);
    }

    private int calculateCorrectCount(List<SubmittedAnswer> answers) {
        return (int) answers.stream()
                .filter(SubmittedAnswer::isCorrect)
                .count();
    }

    private boolean shouldRecordCompletedLearningUnit(
            StudySession session,
            StudySessionCompletionSummary summary
    ) {
        if (summary.answeredCount() <= 0) {
            return false;
        }

        return session.getMode() == ExamMode.EXAM
                || summary.answeredCount() == summary.totalCount();
    }

    private InProgressSessionResponse toInProgressSessionResponse(StudySession session) {
        Exam exam = examService.findPublishedById(session.getExamId());
        String examName = exam != null ? exam.getName() : "";
        int answeredCount = countAnsweredProblems(session.getId());
        int totalCount = session.getPlannedProblemCount() != null
                ? session.getPlannedProblemCount()
                : Math.toIntExact(problemRepository.countActiveByExamId(session.getExamId()));

        return new InProgressSessionResponse(
                session.getId(),
                session.getExamId(),
                examName,
                session.getMode().name(),
                session.getLastIndex(),
                answeredCount,
                totalCount
        );
    }

    private void validateProblemBelongsToSession(Problem problem, Long examId) {
        if (!problem.getExam().getId().equals(examId)) {
            throw new StudySessionException(ExceptionMessage.INVALID_DATA);
        }
    }

    private void validateChoiceBelongsToProblem(Choice choice, Long problemId) {
        if (!choice.getProblem().getId().equals(problemId)) {
            throw new StudySessionException(ExceptionMessage.INVALID_DATA);
        }
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
