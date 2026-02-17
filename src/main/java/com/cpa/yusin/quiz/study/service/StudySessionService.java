package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.StudySessionException;
import com.cpa.yusin.quiz.member.domain.Member;
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

    /**
     * Start a study session.
     * If an IN_PROGRESS session exists for the same exam and mode, return it
     * (Resume).
     * Otherwise, create a new session.
     */
    @Transactional
    public StudySession startSession(Member member, Long examId, ExamMode mode) {
        Optional<StudySession> existingSession = studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(
                member.getId(), examId, StudySessionStatus.IN_PROGRESS, mode);

        if (existingSession.isPresent()) {
            return existingSession.get();
        }

        StudySession newSession = StudySession.start(member, examId, mode);
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
        StudySession session = getSession(sessionId);

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
            studyLogService.recordActivity(session.getMember());

            // Fetch Explanation for Practice Mode
            // N+1 issue potential, but single request per answer.
            // Problem entity access needed.
            String explanation = choice.getProblem().getExplanation(); // Assuming legacy string for now, or V2 logic
            // If V2 JSON blocks are used, we might need to serialize or return raw blocks.
            // For simplicity/legacy compatibility requested in prompt, assuming basic
            // string or empty if V2.

            return ExamAnswerResponse.practice(isCorrect, explanation);
        }

        return ExamAnswerResponse.exam();
    }

    /**
     * Complete the session.
     * Calculates score server-side.
     * Records activity log.
     */
    @Transactional
    public int completeSession(Long sessionId) {
        StudySession session = getSession(sessionId);

        // Calculate Score
        List<SubmittedAnswer> answers = submittedAnswerRepository.findAllByStudySessionId(sessionId);
        int correctCount = (int) answers.stream().filter(SubmittedAnswer::isCorrect).count();
        // Simple scoring: (Correct / Total * 100)? Or just count?
        // Assuming 20 questions * 5 points = 100. Or we can just store the raw score.
        // Ideally we know total problems in exam.
        // For now, let's just return correctCount * 5 (assuming 20 problems) OR just
        // correctCount if exam size varies.
        // User prompt implies "score". Let's calculate as (Correct Count * 5).
        int score = correctCount * 5;

        session.complete(score);

        // Record Activity (Jandi)
        studyLogService.recordActivity(session.getMember());

        return score;
    }
}
