package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.global.exception.StudySessionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.domain.ExamMode;
import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.StudySessionStatus;
import com.cpa.yusin.quiz.study.domain.SubmittedAnswer;
import com.cpa.yusin.quiz.study.event.StudySolvedEvent;
import com.cpa.yusin.quiz.study.service.dto.StudySessionCompletionSummary;
import com.cpa.yusin.quiz.study.service.dto.SubmittedAnswerCorrectnessSnapshot;
import com.cpa.yusin.quiz.study.service.port.StudySessionRepository;
import com.cpa.yusin.quiz.study.service.port.SubmittedAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @InjectMocks
    private StudySessionService studySessionService;

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private SubmittedAnswerRepository submittedAnswerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ClockHolder clockHolder;

    @Mock
    private ChoiceRepository choiceRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ExamService examService;

    @Mock
    private ProblemService problemService;

    @Mock
    private ProblemRepository problemRepository;

    private Member member;
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("세션 시작 - 이어풀기 (Resume)")
    void startSession_whenExists_thenResume() {
        Long memberId = 1L;
        Long examId = 100L;
        ExamMode mode = ExamMode.EXAM;

        StudySession existingSession = StudySession.builder()
                .id(1L)
                .member(member)
                .examId(examId)
                .mode(mode)
                .status(StudySessionStatus.IN_PROGRESS)
                .plannedProblemCount(10)
                .build();

        given(examService.findPublishedById(examId)).willReturn(Exam.builder().id(examId).build());
        given(memberRepository.findByIdWithLock(memberId)).willReturn(Optional.of(member));
        given(studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(memberId, examId,
                StudySessionStatus.IN_PROGRESS, mode)).willReturn(Optional.of(existingSession));

        StudySession session = studySessionService.startSession(memberId, examId, mode);

        assertThat(session).isEqualTo(existingSession);
        verify(studySessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("세션 시작 - legacy 이어풀기는 missing planned count 를 보정한다")
    void startSession_whenLegacyResume_thenBackfillPlannedCount() {
        Long memberId = 1L;
        Long examId = 100L;
        ExamMode mode = ExamMode.EXAM;

        StudySession existingSession = StudySession.builder()
                .id(1L)
                .member(member)
                .examId(examId)
                .mode(mode)
                .status(StudySessionStatus.IN_PROGRESS)
                .build();

        given(examService.findPublishedById(examId)).willReturn(Exam.builder().id(examId).build());
        given(memberRepository.findByIdWithLock(memberId)).willReturn(Optional.of(member));
        given(studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(memberId, examId,
                StudySessionStatus.IN_PROGRESS, mode)).willReturn(Optional.of(existingSession));
        given(submittedAnswerRepository.findAllByStudySessionId(existingSession.getId())).willReturn(List.of(
                SubmittedAnswer.builder().problemId(1L).choiceId(11L).isCorrect(true).build(),
                SubmittedAnswer.builder().problemId(2L).choiceId(22L).isCorrect(false).build()
        ));
        given(problemRepository.countActiveByExamId(examId)).willReturn(1L);

        StudySession session = studySessionService.startSession(memberId, examId, mode);

        assertThat(session.getPlannedProblemCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("세션 시작 - 새 세션 생성 (New)")
    void startSession_whenNew_thenCreate() {
        Long memberId = 1L;
        Long examId = 100L;
        ExamMode mode = ExamMode.EXAM;

        given(examService.findPublishedById(examId)).willReturn(Exam.builder().id(examId).build());
        given(memberRepository.findByIdWithLock(memberId)).willReturn(Optional.of(member));
        given(studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(memberId, examId,
                StudySessionStatus.IN_PROGRESS, mode)).willReturn(Optional.empty());
        given(problemRepository.countActiveByExamId(examId)).willReturn(7L);
        given(clockHolder.getCurrentDateTime()).willReturn(NOW);
        given(studySessionRepository.save(any(StudySession.class))).willAnswer(invocation -> invocation.getArgument(0));

        StudySession session = studySessionService.startSession(memberId, examId, mode);

        assertThat(session.getExamId()).isEqualTo(examId);
        assertThat(session.getMode()).isEqualTo(mode);
        assertThat(session.getStartedAt()).isEqualTo(NOW);
        assertThat(session.getPlannedProblemCount()).isEqualTo(7);
        verify(memberRepository).findByIdWithLock(memberId);
        verify(studySessionRepository).save(any(StudySession.class));
    }

    @Test
    @DisplayName("세션 시작 - 회원이 없으면 예외를 던진다")
    void startSession_whenMemberMissing_thenThrow() {
        Long memberId = 1L;
        Long examId = 100L;

        given(memberRepository.findByIdWithLock(memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> studySessionService.startSession(memberId, examId, ExamMode.EXAM))
                .isInstanceOf(MemberException.class)
                .hasMessage(ExceptionMessage.USER_NOT_FOUND.getMessage());

        verify(studySessionRepository, never())
                .findByMemberIdAndExamIdAndStatusAndMode(any(), any(), any(), any());
    }

    @Test
    @DisplayName("답안 저장 - 첫 저장 (Insert) 시 authoritative isCorrect 를 저장한다")
    void saveAnswer_whenNew_thenInsert() {
        Long sessionId = 1L;
        Long problemId = 10L;
        Long choiceId = 300L;
        int index = 5;

        Long examId = 100L;
        StudySession session = inProgressSession(sessionId, examId, ExamMode.EXAM, 10);
        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

        Exam exam = Exam.builder().id(examId).build();
        Problem problem = Problem.builder().id(problemId).exam(exam).build();
        Choice choice = Choice.builder().id(choiceId).isAnswer(true).problem(problem).build();

        given(examService.findPublishedById(examId)).willReturn(exam);
        given(problemService.findById(problemId)).willReturn(problem);
        given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));
        given(submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId, problemId))
                .willReturn(Optional.empty());

        ArgumentCaptor<SubmittedAnswer> captor = ArgumentCaptor.forClass(SubmittedAnswer.class);

        ExamAnswerResponse response = studySessionService.saveAnswer(member.getId(), sessionId, problemId, choiceId, index);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getIsCorrect()).isNull();
        assertThat(session.getLastIndex()).isEqualTo(index);
        verify(submittedAnswerRepository).save(captor.capture());
        assertThat(captor.getValue().isCorrect()).isTrue();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("답안 저장 - 연습 모드도 authoritative isCorrect 를 저장하고 첫 제출만 로그 반영")
    void saveAnswer_whenPractice_thenReturnFeedbackAndPublishEventOnce() {
        Long sessionId = 1L;
        Long problemId = 10L;
        Long choiceId = 300L;

        StudySession session = inProgressSession(sessionId, 100L, ExamMode.PRACTICE, 10);

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

        Exam exam = Exam.builder().id(100L).build();
        Problem problem = Problem.builder().id(problemId).exam(exam).explanation("Test Explanation").build();
        Choice choice = Choice.builder().id(choiceId).isAnswer(false).problem(problem).build();

        given(examService.findPublishedById(100L)).willReturn(exam);
        given(problemService.findById(problemId)).willReturn(problem);
        given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));
        given(submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId, problemId))
                .willReturn(Optional.empty());

        ArgumentCaptor<SubmittedAnswer> captor = ArgumentCaptor.forClass(SubmittedAnswer.class);

        ExamAnswerResponse response = studySessionService.saveAnswer(member.getId(), sessionId, problemId, choiceId, 1);

        assertThat(response.getIsCorrect()).isFalse();
        assertThat(response.getExplanation()).isEqualTo("Test Explanation");
        verify(submittedAnswerRepository).save(captor.capture());
        assertThat(captor.getValue().isCorrect()).isFalse();
        verify(eventPublisher).publishEvent(any(StudySolvedEvent.class));
    }

    @Test
    @DisplayName("답안 저장 - 연습 모드에서 같은 문제 답 변경은 잔디를 재집계하지 않는다")
    void saveAnswer_whenPracticeAnswerUpdated_thenDoNotLogAgain() {
        Long sessionId = 1L;
        Long problemId = 10L;
        Long choiceId = 301L;

        StudySession session = inProgressSession(sessionId, 100L, ExamMode.PRACTICE, 10);
        SubmittedAnswer existingAnswer = SubmittedAnswer.builder()
                .id(50L)
                .problemId(problemId)
                .choiceId(100L)
                .isCorrect(true)
                .build();

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

        Exam exam = Exam.builder().id(100L).build();
        Problem problem = Problem.builder().id(problemId).exam(exam).explanation("Test Explanation").build();
        Choice choice = Choice.builder().id(choiceId).isAnswer(false).problem(problem).build();

        given(examService.findPublishedById(100L)).willReturn(exam);
        given(problemService.findById(problemId)).willReturn(problem);
        given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));
        given(submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId, problemId))
                .willReturn(Optional.of(existingAnswer));

        ExamAnswerResponse response = studySessionService.saveAnswer(member.getId(), sessionId, problemId, choiceId, 1);

        assertThat(response.getIsCorrect()).isFalse();
        assertThat(existingAnswer.isCorrect()).isFalse();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("답안 저장 - 수정 (Update)")
    void saveAnswer_whenExists_thenUpdate() {
        Long sessionId = 1L;
        Long problemId = 10L;
        Long choiceId = 400L;
        int index = 6;

        Long examId = 200L;
        StudySession session = inProgressSession(sessionId, examId, ExamMode.EXAM, 10);
        SubmittedAnswer existingAnswer = SubmittedAnswer.builder()
                .id(50L)
                .problemId(problemId)
                .choiceId(100L)
                .isCorrect(true)
                .build();

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

        Exam exam = Exam.builder().id(examId).build();
        Problem problem = Problem.builder().id(problemId).exam(exam).build();
        Choice choice = Choice.builder().id(choiceId).isAnswer(false).problem(problem).build();

        given(examService.findPublishedById(examId)).willReturn(exam);
        given(problemService.findById(problemId)).willReturn(problem);
        given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));
        given(submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId, problemId))
                .willReturn(Optional.of(existingAnswer));

        studySessionService.saveAnswer(member.getId(), sessionId, problemId, choiceId, index);

        assertThat(session.getLastIndex()).isEqualTo(index);
        assertThat(existingAnswer.getChoiceId()).isEqualTo(choiceId);
        assertThat(existingAnswer.isCorrect()).isFalse();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("시험 종료 - counts 기반 요약 및 잔디 기록 (실전 모드)")
    void completeSession_exam_shouldCalculateScoreAndLog() {
        Long sessionId = 1L;
        StudySession session = inProgressSession(sessionId, 100L, ExamMode.EXAM, 5);

        List<SubmittedAnswer> answers = List.of(
                SubmittedAnswer.builder().problemId(1L).choiceId(11L).isCorrect(true).build(),
                SubmittedAnswer.builder().problemId(2L).choiceId(22L).isCorrect(true).build(),
                SubmittedAnswer.builder().problemId(3L).choiceId(33L).isCorrect(false).build()
        );

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
        given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(answers);
        given(submittedAnswerRepository.findCorrectnessSnapshotsByStudySessionId(sessionId)).willReturn(List.of(
                new SubmittedAnswerCorrectnessSnapshot(1L, 11L, true),
                new SubmittedAnswerCorrectnessSnapshot(2L, 22L, true),
                new SubmittedAnswerCorrectnessSnapshot(3L, 33L, false)
        ));
        given(clockHolder.getCurrentDateTime()).willReturn(NOW);

        StudySessionCompletionSummary summary = studySessionService.completeSession(member.getId(), sessionId);

        assertThat(summary.correctCount()).isEqualTo(2);
        assertThat(summary.totalCount()).isEqualTo(5);
        assertThat(summary.answeredCount()).isEqualTo(3);
        assertThat(summary.unansweredCount()).isEqualTo(2);
        assertThat(summary.finalScore()).isEqualTo(2);
        assertThat(session.getFinishedAt()).isEqualTo(NOW);
        assertThat(session.getStatus()).isEqualTo(StudySessionStatus.COMPLETED);
        assertThat(session.getCurrentScore()).isEqualTo(2);
        verify(examService, never()).findPublishedById(anyLong());
        verify(eventPublisher).publishEvent(any(StudySolvedEvent.class));
    }

    @Test
    @DisplayName("시험 종료 - 연습 모드도 같은 요약 계약을 쓰고 finish 에서는 로그를 추가하지 않는다")
    void completeSession_practice_shouldNotLog() {
        Long sessionId = 1L;
        StudySession session = inProgressSession(sessionId, 100L, ExamMode.PRACTICE, 4);

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
        given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(List.of(
                SubmittedAnswer.builder().problemId(1L).choiceId(11L).isCorrect(true).build(),
                SubmittedAnswer.builder().problemId(2L).choiceId(22L).isCorrect(false).build()
        ));
        given(submittedAnswerRepository.findCorrectnessSnapshotsByStudySessionId(sessionId)).willReturn(List.of(
                new SubmittedAnswerCorrectnessSnapshot(1L, 11L, true),
                new SubmittedAnswerCorrectnessSnapshot(2L, 22L, false)
        ));
        given(clockHolder.getCurrentDateTime()).willReturn(NOW);

        StudySessionCompletionSummary summary = studySessionService.completeSession(member.getId(), sessionId);

        assertThat(summary.correctCount()).isEqualTo(1);
        assertThat(summary.totalCount()).isEqualTo(4);
        assertThat(summary.answeredCount()).isEqualTo(2);
        assertThat(summary.unansweredCount()).isEqualTo(2);
        assertThat(session.getFinishedAt()).isEqualTo(NOW);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("시험 종료 - 완료된 세션 재요청은 동일한 요약을 재응답하고 로그를 다시 적재하지 않는다")
    void completeSession_whenSessionAlreadyCompleted_thenReturnSummary() {
        Long sessionId = 1L;

        StudySession session = StudySession.builder()
                .id(sessionId)
                .member(member)
                .examId(100L)
                .mode(ExamMode.EXAM)
                .status(StudySessionStatus.COMPLETED)
                .plannedProblemCount(5)
                .currentScore(2)
                .build();

        List<SubmittedAnswer> answers = List.of(
                SubmittedAnswer.builder().problemId(1L).choiceId(11L).isCorrect(true).build(),
                SubmittedAnswer.builder().problemId(2L).choiceId(22L).isCorrect(true).build(),
                SubmittedAnswer.builder().problemId(3L).choiceId(33L).isCorrect(false).build()
        );

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
        given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(answers);
        given(submittedAnswerRepository.findCorrectnessSnapshotsByStudySessionId(sessionId)).willReturn(List.of(
                new SubmittedAnswerCorrectnessSnapshot(1L, 11L, true),
                new SubmittedAnswerCorrectnessSnapshot(2L, 22L, true),
                new SubmittedAnswerCorrectnessSnapshot(3L, 33L, false)
        ));

        StudySessionCompletionSummary summary = studySessionService.completeSession(member.getId(), sessionId);

        assertThat(summary.correctCount()).isEqualTo(2);
        assertThat(summary.totalCount()).isEqualTo(5);
        assertThat(summary.answeredCount()).isEqualTo(3);
        assertThat(summary.unansweredCount()).isEqualTo(2);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("시험 종료 - legacy completed 세션은 missing planned count 를 보정한 뒤 재응답한다")
    void completeSession_whenCompletedLegacySessionMissingPlannedCount_thenBackfill() {
        Long sessionId = 1L;

        StudySession session = StudySession.builder()
                .id(sessionId)
                .member(member)
                .examId(100L)
                .mode(ExamMode.EXAM)
                .status(StudySessionStatus.COMPLETED)
                .build();

        List<SubmittedAnswer> answers = List.of(
                SubmittedAnswer.builder().problemId(1L).choiceId(11L).isCorrect(true).build(),
                SubmittedAnswer.builder().problemId(2L).choiceId(22L).isCorrect(false).build(),
                SubmittedAnswer.builder().problemId(3L).choiceId(33L).isCorrect(false).build()
        );

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
        given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(answers);
        given(submittedAnswerRepository.findCorrectnessSnapshotsByStudySessionId(sessionId)).willReturn(List.of(
                new SubmittedAnswerCorrectnessSnapshot(1L, 11L, true),
                new SubmittedAnswerCorrectnessSnapshot(2L, 22L, false),
                new SubmittedAnswerCorrectnessSnapshot(3L, 33L, false)
        ));
        given(problemRepository.countActiveByExamId(100L)).willReturn(2L);

        StudySessionCompletionSummary summary = studySessionService.completeSession(member.getId(), sessionId);

        assertThat(summary.correctCount()).isEqualTo(1);
        assertThat(summary.totalCount()).isEqualTo(3);
        assertThat(summary.answeredCount()).isEqualTo(3);
        assertThat(summary.unansweredCount()).isZero();
        assertThat(session.getPlannedProblemCount()).isEqualTo(3);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("시험 종료 - persisted correctness 가 깨진 경우 batch join 결과로 보정한다")
    void completeSession_whenPersistedCorrectnessIsCorrupted_thenUseFallback() {
        Long sessionId = 1L;
        StudySession session = inProgressSession(sessionId, 100L, ExamMode.EXAM, 2);

        List<SubmittedAnswer> answers = List.of(
                SubmittedAnswer.builder().problemId(1L).choiceId(11L).isCorrect(false).build(),
                SubmittedAnswer.builder().problemId(2L).choiceId(22L).isCorrect(false).build()
        );

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
        given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(answers);
        given(submittedAnswerRepository.findCorrectnessSnapshotsByStudySessionId(sessionId)).willReturn(List.of(
                new SubmittedAnswerCorrectnessSnapshot(1L, 11L, true),
                new SubmittedAnswerCorrectnessSnapshot(2L, 22L, false)
        ));
        given(clockHolder.getCurrentDateTime()).willReturn(NOW);

        StudySessionCompletionSummary summary = studySessionService.completeSession(member.getId(), sessionId);

        assertThat(summary.correctCount()).isEqualTo(1);
        assertThat(summary.finalScore()).isEqualTo(1);
        verify(eventPublisher).publishEvent(any(StudySolvedEvent.class));
    }

    @Test
    @DisplayName("세션 시작 - 삭제된 시험이면 시작/이어풀기 모두 차단")
    void startSession_whenExamIsDeleted_thenThrow() {
        Long memberId = 1L;
        Long examId = 100L;

        given(memberRepository.findByIdWithLock(memberId)).willReturn(Optional.of(member));
        given(examService.findPublishedById(examId))
                .willThrow(new com.cpa.yusin.quiz.global.exception.ExamException(ExceptionMessage.EXAM_NOT_FOUND));

        assertThatThrownBy(() -> studySessionService.startSession(memberId, examId, ExamMode.EXAM))
                .isInstanceOf(com.cpa.yusin.quiz.global.exception.ExamException.class)
                .hasMessage(ExceptionMessage.EXAM_NOT_FOUND.getMessage());

        verify(studySessionRepository, never())
                .findByMemberIdAndExamIdAndStatusAndMode(any(), any(), any(), any());
    }

    @Test
    @DisplayName("답안 저장 - 요청 problemId 가 세션 exam 에 속하지 않으면 차단")
    void saveAnswer_whenProblemDoesNotBelongToSessionExam_thenThrow() {
        Long sessionId = 1L;
        Long sessionExamId = 100L;
        Long problemId = 10L;

        StudySession session = inProgressSession(sessionId, sessionExamId, ExamMode.EXAM, 10);
        Exam sessionExam = Exam.builder().id(sessionExamId).build();
        Exam otherExam = Exam.builder().id(999L).build();
        Problem problem = Problem.builder().id(problemId).exam(otherExam).build();

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
        given(examService.findPublishedById(sessionExamId)).willReturn(sessionExam);
        given(problemService.findById(problemId)).willReturn(problem);

        assertThatThrownBy(() -> studySessionService.saveAnswer(member.getId(), sessionId, problemId, 1L, 0))
                .isInstanceOf(StudySessionException.class)
                .hasMessage(ExceptionMessage.INVALID_DATA.getMessage());
    }

    @Test
    @DisplayName("답안 저장 - 요청 problemId 와 choice 소속 problem 이 다르면 차단")
    void saveAnswer_whenChoiceDoesNotBelongToProblem_thenThrow() {
        Long sessionId = 1L;
        Long examId = 100L;
        Long problemId = 10L;
        Long choiceId = 300L;

        StudySession session = inProgressSession(sessionId, examId, ExamMode.EXAM, 10);
        Exam exam = Exam.builder().id(examId).build();
        Problem requestedProblem = Problem.builder().id(problemId).exam(exam).build();
        Problem otherProblem = Problem.builder().id(999L).exam(exam).build();
        Choice choice = Choice.builder().id(choiceId).problem(otherProblem).isAnswer(true).build();

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
        given(examService.findPublishedById(examId)).willReturn(exam);
        given(problemService.findById(problemId)).willReturn(requestedProblem);
        given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));

        assertThatThrownBy(() -> studySessionService.saveAnswer(member.getId(), sessionId, problemId, choiceId, 1))
                .isInstanceOf(StudySessionException.class)
                .hasMessage(ExceptionMessage.INVALID_DATA.getMessage());
    }

    @Test
    @DisplayName("답안 저장 - 삭제된 문제 체인이면 차단")
    void saveAnswer_whenProblemHierarchyDeleted_thenThrow() {
        Long sessionId = 1L;
        Long examId = 100L;
        Long problemId = 10L;

        StudySession session = inProgressSession(sessionId, examId, ExamMode.EXAM, 10);
        Exam exam = Exam.builder().id(examId).build();

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
        given(examService.findPublishedById(examId)).willReturn(exam);
        given(problemService.findById(problemId))
                .willThrow(new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));

        assertThatThrownBy(() -> studySessionService.saveAnswer(member.getId(), sessionId, problemId, 1L, 0))
                .isInstanceOf(ProblemException.class)
                .hasMessage(ExceptionMessage.PROBLEM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("답안 저장 - 다른 회원의 세션이면 거부한다")
    void saveAnswer_whenSessionOwnedByAnotherMember_thenThrow() {
        Long sessionId = 1L;
        Long examId = 100L;
        Long problemId = 10L;

        Member otherMember = Member.builder()
                .id(999L)
                .email("other@example.com")
                .role(Role.USER)
                .build();
        StudySession session = StudySession.builder()
                .id(sessionId)
                .member(otherMember)
                .examId(examId)
                .mode(ExamMode.EXAM)
                .status(StudySessionStatus.IN_PROGRESS)
                .plannedProblemCount(10)
                .build();

        given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

        assertThatThrownBy(() -> studySessionService.saveAnswer(member.getId(), sessionId, problemId, 1L, 0))
                .isInstanceOf(MemberException.class)
                .hasMessage(ExceptionMessage.NO_AUTHORIZATION.getMessage());
    }

    private StudySession inProgressSession(Long sessionId, Long examId, ExamMode mode, int plannedProblemCount) {
        return StudySession.builder()
                .id(sessionId)
                .member(member)
                .examId(examId)
                .mode(mode)
                .status(StudySessionStatus.IN_PROGRESS)
                .plannedProblemCount(plannedProblemCount)
                .build();
    }
}
