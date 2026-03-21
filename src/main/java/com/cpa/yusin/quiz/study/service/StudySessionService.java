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
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.domain.*;
import com.cpa.yusin.quiz.study.event.StudySolvedEvent;
import com.cpa.yusin.quiz.study.service.port.StudySessionRepository;
import com.cpa.yusin.quiz.study.service.port.SubmittedAnswerRepository;
import com.cpa.yusin.quiz.study.service.dto.StudySessionCompletionSummary;
import com.cpa.yusin.quiz.study.service.dto.SubmittedAnswerCorrectnessSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final SubmittedAnswerRepository submittedAnswerRepository;
    private final ChoiceRepository choiceRepository;
    private final MemberRepository memberRepository;
    private final ExamService examService;
    private final ProblemService problemService;
    private final ProblemRepository problemRepository;
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
        Member lockedMember = memberRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new MemberException(ExceptionMessage.USER_NOT_FOUND));

        examService.findPublishedById(examId);

        Optional<StudySession> existingSession = studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(
                memberId, examId, StudySessionStatus.IN_PROGRESS, mode);

        if (existingSession.isPresent()) {
            StudySession resumedSession = existingSession.get();
            backfillPlannedProblemCountIfMissing(resumedSession, examId, countAnsweredProblems(resumedSession.getId()));
            return resumedSession;
        }

        LocalDateTime now = clockHolder.getCurrentDateTime();
        int plannedProblemCount = Math.toIntExact(problemRepository.countActiveByExamId(examId));
        StudySession newSession = StudySession.start(lockedMember, examId, mode, now, plannedProblemCount);
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
        StudySession session = studySessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new StudySessionException(ExceptionMessage.SESSION_NOT_FOUND));
        validateOwnership(session, memberId);
        validateInProgress(session);

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

            if (session.getMode() == ExamMode.PRACTICE) {
                eventPublisher.publishEvent(new StudySolvedEvent(session.getMember().getId(), 1));
            }
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
        StudySession session = studySessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new StudySessionException(ExceptionMessage.SESSION_NOT_FOUND));
        validateOwnership(session, memberId);

        List<SubmittedAnswer> answers = submittedAnswerRepository.findAllByStudySessionId(sessionId);
        backfillPlannedProblemCountIfMissing(session, session.getExamId(), answers.size());
        StudySessionCompletionSummary summary = buildCompletionSummary(session, answers);

        if (!session.isInProgress()) {
            return summary;
        }

        LocalDateTime now = clockHolder.getCurrentDateTime();
        session.complete(summary.correctCount(), now);

        if (session.getMode() == ExamMode.EXAM) {
            eventPublisher.publishEvent(new StudySolvedEvent(session.getMember().getId(), summary.answeredCount()));
        }

        return summary;
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
        int correctCount = calculateCorrectCount(session.getId(), answers);
        int unansweredCount = Math.max(totalCount - answeredCount, 0);

        return new StudySessionCompletionSummary(correctCount, totalCount, answeredCount, unansweredCount);
    }

    /**
     * SubmittedAnswer.isCorrect is the normal finish fast path because saveAnswer
     * persists the evaluated correctness for both PRACTICE and EXAM.
     * We still run a single batched verification query so legacy or corrupted rows
     * can fall back to a choice-join based recalculation without introducing N+1.
     */
    private int calculateCorrectCount(Long sessionId, List<SubmittedAnswer> answers) {
        int persistedCorrectCount = (int) answers.stream()
                .filter(SubmittedAnswer::isCorrect)
                .count();

        if (answers.isEmpty()) {
            return persistedCorrectCount;
        }

        List<SubmittedAnswerCorrectnessSnapshot> snapshots = submittedAnswerRepository
                .findCorrectnessSnapshotsByStudySessionId(sessionId);

        if (snapshots.size() != answers.size()) {
            return persistedCorrectCount;
        }

        Map<Long, SubmittedAnswerCorrectnessSnapshot> snapshotByProblemId = snapshots.stream()
                .collect(Collectors.toMap(SubmittedAnswerCorrectnessSnapshot::problemId, Function.identity()));

        boolean mismatchDetected = false;
        int recalculatedCorrectCount = 0;

        for (SubmittedAnswer answer : answers) {
            SubmittedAnswerCorrectnessSnapshot snapshot = snapshotByProblemId.get(answer.getProblemId());

            if (snapshot == null
                    || !Objects.equals(snapshot.choiceId(), answer.getChoiceId())
                    || snapshot.authoritativeCorrect() == null) {
                return persistedCorrectCount;
            }

            if (!Objects.equals(snapshot.authoritativeCorrect(), answer.isCorrect())) {
                mismatchDetected = true;
            }

            if (Boolean.TRUE.equals(snapshot.authoritativeCorrect())) {
                recalculatedCorrectCount++;
            }
        }

        return mismatchDetected ? recalculatedCorrectCount : persistedCorrectCount;
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
