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
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.domain.*;
import com.cpa.yusin.quiz.study.event.StudySolvedEvent;
import com.cpa.yusin.quiz.study.service.port.StudySessionRepository;
import com.cpa.yusin.quiz.study.service.port.SubmittedAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class StudySessionServiceTest {

        @InjectMocks
        private StudySessionService studySessionService;

        @Mock
        private StudySessionRepository studySessionRepository;

        @Mock
        private SubmittedAnswerRepository submittedAnswerRepository;

        // Use ApplicationEventPublisher instead of StudyLogService
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

        private Member member;
        private final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

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
                // given
                Long memberId = 1L;
                Long examId = 100L;
                ExamMode mode = ExamMode.EXAM;

                StudySession existingSession = StudySession.builder()
                                .id(1L).member(member).examId(examId).mode(mode).status(StudySessionStatus.IN_PROGRESS)
                                .build();

                given(examService.findById(examId)).willReturn(Exam.builder().id(examId).build());
                given(memberRepository.findByIdWithLock(memberId)).willReturn(Optional.of(member));
                given(studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(memberId, examId,
                                StudySessionStatus.IN_PROGRESS, mode))
                                .willReturn(Optional.of(existingSession));

                // when
                StudySession session = studySessionService.startSession(memberId, examId, mode);

                // then
                assertThat(session).isEqualTo(existingSession);
                verify(studySessionRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("세션 시작 - 새 세션 생성 (New)")
        void startSession_whenNew_thenCreate() {
                // given
                Long memberId = 1L;
                Long examId = 100L;
                ExamMode mode = ExamMode.EXAM;

                given(examService.findById(examId)).willReturn(Exam.builder().id(examId).build());
                given(memberRepository.findByIdWithLock(memberId)).willReturn(Optional.of(member));
                given(studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(memberId, examId,
                                StudySessionStatus.IN_PROGRESS, mode))
                                .willReturn(Optional.empty());

                given(clockHolder.getCurrentDateTime()).willReturn(NOW);

                given(studySessionRepository.save(any(StudySession.class)))
                                .willAnswer(invocation -> invocation.getArgument(0));

                // when
                StudySession session = studySessionService.startSession(memberId, examId, mode);

                // then
                assertThat(session.getExamId()).isEqualTo(examId);
                assertThat(session.getMode()).isEqualTo(mode);
                assertThat(session.getStartedAt()).isEqualTo(NOW);
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
        @DisplayName("답안 저장 - 첫 저장 (Insert)")
        void saveAnswer_whenNew_thenInsert() {
                // given
                Long sessionId = 1L;
                Long problemId = 10L;
                Long choiceId = 300L;
                boolean correct = true;
                int index = 5;

                Long examId = 100L;
                StudySession session = StudySession.builder().id(sessionId).examId(examId).mode(ExamMode.EXAM).build();
                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

                Exam exam = Exam.builder().id(examId).build();
                Problem problem = Problem.builder().id(problemId).exam(exam).build();
                Choice choice = Choice.builder().id(choiceId).isAnswer(correct).problem(problem).build();

                given(examService.findById(examId)).willReturn(exam);
                given(problemService.findById(problemId)).willReturn(problem);
                given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));

                given(submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId, problemId))
                                .willReturn(Optional.empty());

                // when
                ExamAnswerResponse response = studySessionService.saveAnswer(sessionId, problemId, choiceId, index);

                // then
                assertThat(response.isSuccess()).isTrue();
                assertThat(response.getIsCorrect()).isNull();
                assertThat(session.getLastIndex()).isEqualTo(index);
                verify(submittedAnswerRepository).save(any(SubmittedAnswer.class));
                // Verify EVENT is NOT published for EXAM mode answer save
                verify(eventPublisher, org.mockito.Mockito.never()).publishEvent(any());
        }

        @Test
        @DisplayName("답안 저장 - 연습 모드 피드백 확인")
        void saveAnswer_whenPractice_thenReturnFeedback() {
                // given
                Long sessionId = 1L;
                Long problemId = 10L;
                Long choiceId = 300L;
                boolean correct = false;

                StudySession session = StudySession.builder().id(sessionId).mode(ExamMode.PRACTICE)
                                .member(member)
                                .examId(100L)
                                .build();

                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

                Exam exam = Exam.builder().id(100L).build();
                Problem problem = Problem.builder().id(problemId).exam(exam).explanation("Test Explanation").build();
                Choice choice = Choice.builder().id(choiceId).isAnswer(correct).problem(problem).build();

                given(examService.findById(100L)).willReturn(exam);
                given(problemService.findById(problemId)).willReturn(problem);
                given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));

                // when
                ExamAnswerResponse response = studySessionService.saveAnswer(sessionId, problemId, choiceId, 1);

                // then
                assertThat(response.getIsCorrect()).isFalse();
                assertThat(response.getExplanation()).isEqualTo("Test Explanation");
                // Verify EVENT is published
                verify(eventPublisher).publishEvent(any(StudySolvedEvent.class));
        }

        @Test
        @DisplayName("답안 저장 - 수정 (Update)")
        void saveAnswer_whenExists_thenUpdate() {
                // given
                Long sessionId = 1L;
                Long problemId = 10L;
                Long choiceId = 400L;
                boolean correct = false;
                int index = 6;

                Long examId = 200L;
                StudySession session = StudySession.builder().id(sessionId).examId(examId).mode(ExamMode.EXAM).build();
                SubmittedAnswer existingAnswer = SubmittedAnswer.builder()
                                .id(50L).choiceId(100L).isCorrect(true).build();

                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

                Exam exam = Exam.builder().id(examId).build();
                Problem problem = Problem.builder().id(problemId).exam(exam).build();
                Choice choice = Choice.builder().id(choiceId).isAnswer(correct).problem(problem).build();

                given(examService.findById(examId)).willReturn(exam);
                given(problemService.findById(problemId)).willReturn(problem);
                given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));

                given(submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId, problemId))
                                .willReturn(Optional.of(existingAnswer));

                // when
                studySessionService.saveAnswer(sessionId, problemId, choiceId, index);

                // then
                assertThat(session.getLastIndex()).isEqualTo(index);
                assertThat(existingAnswer.getChoiceId()).isEqualTo(choiceId);
                assertThat(existingAnswer.isCorrect()).isEqualTo(correct);
        }

        @Test
        @DisplayName("시험 종료 - 점수 계산 및 잔디 기록 (실전 모드)")
        void completeSession_exam_shouldCalculateScoreAndLog() {
                // given
                Long sessionId = 1L;
                StudySession session = StudySession.builder().id(sessionId).mode(ExamMode.EXAM).member(member).build();

                List<SubmittedAnswer> answers = List.of(
                                SubmittedAnswer.builder().isCorrect(true).build(),
                                SubmittedAnswer.builder().isCorrect(true).build(),
                                SubmittedAnswer.builder().isCorrect(false).build());

                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
                given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(answers);
                given(clockHolder.getCurrentDateTime()).willReturn(NOW);

                // when
                int finalScore = studySessionService.completeSession(sessionId);

                // then
                assertThat(finalScore).isEqualTo(2);
                assertThat(session.getFinishedAt()).isEqualTo(NOW);
                verify(examService, never()).findById(anyLong());
                // Verify EVENT is published
                verify(eventPublisher).publishEvent(any(StudySolvedEvent.class));
                assertThat(session.getStatus()).isEqualTo(StudySessionStatus.COMPLETED);
        }

        @Test
        @DisplayName("시험 종료 - 연습 모드는 잔디 기록 스킵 (이미 건별 기록됨)")
        void completeSession_practice_shouldNotLog() {
                // given
                Long sessionId = 1L;
                StudySession session = StudySession.builder().id(sessionId).mode(ExamMode.PRACTICE).member(member)
                                .build();

                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
                given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(List.of());
                given(clockHolder.getCurrentDateTime()).willReturn(NOW);

                // when
                studySessionService.completeSession(sessionId);

                // then
                assertThat(session.getFinishedAt()).isEqualTo(NOW);
                verify(examService, never()).findById(anyLong());
                verify(eventPublisher, org.mockito.Mockito.never()).publishEvent(any());
        }

        @Test
        @DisplayName("세션 시작 - 삭제된 시험이면 시작/이어풀기 모두 차단")
        void startSession_whenExamIsDeleted_thenThrow() {
                Long memberId = 1L;
                Long examId = 100L;

                given(memberRepository.findByIdWithLock(memberId)).willReturn(Optional.of(member));
                given(examService.findById(examId))
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

                StudySession session = StudySession.builder().id(sessionId).examId(sessionExamId).mode(ExamMode.EXAM)
                                .build();
                Exam sessionExam = Exam.builder().id(sessionExamId).build();
                Exam otherExam = Exam.builder().id(999L).build();
                Problem problem = Problem.builder().id(problemId).exam(otherExam).build();

                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
                given(examService.findById(sessionExamId)).willReturn(sessionExam);
                given(problemService.findById(problemId)).willReturn(problem);

                assertThatThrownBy(() -> studySessionService.saveAnswer(sessionId, problemId, 1L, 0))
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

                StudySession session = StudySession.builder().id(sessionId).examId(examId).mode(ExamMode.EXAM).build();
                Exam exam = Exam.builder().id(examId).build();
                Problem requestedProblem = Problem.builder().id(problemId).exam(exam).build();
                Problem otherProblem = Problem.builder().id(999L).exam(exam).build();
                Choice choice = Choice.builder().id(choiceId).problem(otherProblem).isAnswer(true).build();

                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
                given(examService.findById(examId)).willReturn(exam);
                given(problemService.findById(problemId)).willReturn(requestedProblem);
                given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));

                assertThatThrownBy(() -> studySessionService.saveAnswer(sessionId, problemId, choiceId, 1))
                                .isInstanceOf(StudySessionException.class)
                                .hasMessage(ExceptionMessage.INVALID_DATA.getMessage());
        }

        @Test
        @DisplayName("답안 저장 - 삭제된 문제 체인이면 차단")
        void saveAnswer_whenProblemHierarchyDeleted_thenThrow() {
                Long sessionId = 1L;
                Long examId = 100L;
                Long problemId = 10L;

                StudySession session = StudySession.builder().id(sessionId).examId(examId).mode(ExamMode.EXAM).build();
                Exam exam = Exam.builder().id(examId).build();

                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
                given(examService.findById(examId)).willReturn(exam);
                given(problemService.findById(problemId))
                                .willThrow(new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));

                assertThatThrownBy(() -> studySessionService.saveAnswer(sessionId, problemId, 1L, 0))
                                .isInstanceOf(ProblemException.class)
                                .hasMessage(ExceptionMessage.PROBLEM_NOT_FOUND.getMessage());
        }
}
