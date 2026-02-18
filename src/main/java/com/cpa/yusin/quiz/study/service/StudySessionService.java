package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.StudySessionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.domain.*;
import com.cpa.yusin.quiz.study.service.port.StudySessionRepository;
import com.cpa.yusin.quiz.study.service.port.SubmittedAnswerRepository;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final SubmittedAnswerRepository submittedAnswerRepository;
    private final StudyLogService studyLogService; // Inject for activity recording
    private final ChoiceRepository choiceRepository;
    private final MemberRepository memberRepository;

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
        StudySession newSession = StudySession.start(memberRef, examId, mode);
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
            try {
                studyLogService.recordActivity(session.getMember().getId());
            } catch (Exception e) {
                log.error("Failed to record activity for member {}", session.getMember().getId(), e);
                // Do not throw, continue to return response
            }

            // Fetch Explanation for Practice Mode
            try {
                // N+1 issue potential, but single request per answer.
                // Problem entity access needed.
                String explanation = choice.getProblem().getExplanation(); // Assuming legacy string for now, or V2
                                                                           // logic
                return ExamAnswerResponse.practice(isCorrect, explanation);
            } catch (Exception e) {
                log.error("Failed to fetch explanation for choice {}", choiceId, e);
                return ExamAnswerResponse.practice(isCorrect, "해설을 불러올 수 없습니다.");
            }
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
        int correctCount = (int) answers.stream().filter(SubmittedAnswer::isCorrect).count();
        int score = correctCount * 5;

        session.complete(score);

        // Record Activity (Jandi)
        // Optimization: Batch Update for Exam Mode
        // Practice mode updates incrementally in saveAnswer, so we might skip here or
        // double check.
        // Requirement: "Real Mode ... POST /finish ... Update DailyStudyLog at once +N"
        // Requirement: "Practice Mode ... POST /answer ... Update ... immediately +1"

        if (session.getMode() == ExamMode.EXAM) {
            // For Exam Mode, we count the number of distinct questions solved in this
            // session.
            // 'answers' contains all answers.
            // We should increment by the number of answers submitted (or correct ones?
            // Prompt says "solvedCount").
            // Usually "solved" means correctly answered, but "Contribution Graph" often
            // just means "Attempted".
            // Prompt: "currently... 100 Update queries... load concern" -> implies counting
            // attempts.
            // Let's count *attempts* (answers.size()).
            int solvedCount = answers.size();
            studyLogService.recordActivity(session.getMember().getId(), solvedCount);
        }
        // Practice Mode: Already updated incrementally in saveAnswer.

        return score;
    }
}
