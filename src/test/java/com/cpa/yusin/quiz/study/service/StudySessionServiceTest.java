package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.domain.*;
import com.cpa.yusin.quiz.study.service.port.StudySessionRepository;
import com.cpa.yusin.quiz.study.service.port.SubmittedAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StudySessionServiceTest {

        @InjectMocks
        private StudySessionService studySessionService;

        @Mock
        private StudySessionRepository studySessionRepository;

        @Mock
        private SubmittedAnswerRepository submittedAnswerRepository;

        @Mock
        private StudyLogService studyLogService;

        @Mock
        private ChoiceRepository choiceRepository;

        @Mock
        private MemberRepository memberRepository;

        private Member member;

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

                given(studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(memberId, examId,
                                StudySessionStatus.IN_PROGRESS, mode))
                                .willReturn(Optional.empty());

                given(memberRepository.getReferenceById(memberId)).willReturn(member);

                given(studySessionRepository.save(any(StudySession.class)))
                                .willAnswer(invocation -> invocation.getArgument(0));

                // when
                StudySession session = studySessionService.startSession(memberId, examId, mode);

                // then
                assertThat(session.getExamId()).isEqualTo(examId);
                assertThat(session.getMode()).isEqualTo(mode);
                verify(memberRepository).getReferenceById(memberId);
                verify(studySessionRepository).save(any(StudySession.class));
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

                StudySession session = StudySession.builder().id(sessionId).mode(ExamMode.EXAM).build();
                // Use findByIdWithLock for locking
                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

                // Choice Mock
                Choice choice = Choice.builder()
                                .id(choiceId).isAnswer(correct).build();
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
                verify(studyLogService, org.mockito.Mockito.never()).recordActivity(any());
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
                                .member(member).build();

                // Use findByIdWithLock
                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

                Problem problem = Problem.builder().explanation("Test Explanation").build();
                Choice choice = Choice.builder().id(choiceId).isAnswer(correct).problem(problem).build();

                given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));

                // when
                ExamAnswerResponse response = studySessionService.saveAnswer(sessionId, problemId, choiceId, 1);

                // then
                assertThat(response.getIsCorrect()).isFalse();
                assertThat(response.getExplanation()).isEqualTo("Test Explanation");
                verify(studyLogService).recordActivity(member.getId());
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

                StudySession session = StudySession.builder().id(sessionId).mode(ExamMode.EXAM).build();
                SubmittedAnswer existingAnswer = SubmittedAnswer.builder()
                                .id(50L).choiceId(100L).isCorrect(true).build();

                // Use findByIdWithLock
                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));

                Choice choice = Choice.builder().id(choiceId).isAnswer(correct).build();
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

                // Use findByIdWithLock
                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
                given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(answers);

                // when
                int finalScore = studySessionService.completeSession(sessionId);

                // then
                assertThat(finalScore).isEqualTo(10);
                verify(studyLogService).recordActivity(member.getId(), 3);
                assertThat(session.getStatus()).isEqualTo(StudySessionStatus.COMPLETED);
        }

        @Test
        @DisplayName("시험 종료 - 연습 모드는 잔디 기록 스킵 (이미 건별 기록됨)")
        void completeSession_practice_shouldNotLog() {
                // given
                Long sessionId = 1L;
                StudySession session = StudySession.builder().id(sessionId).mode(ExamMode.PRACTICE).member(member)
                                .build();

                // Use findByIdWithLock
                given(studySessionRepository.findByIdWithLock(sessionId)).willReturn(Optional.of(session));
                given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(List.of());

                // when
                studySessionService.completeSession(sessionId);

                // then
                verify(studyLogService, org.mockito.Mockito.never()).recordActivity(any());
                verify(studyLogService, org.mockito.Mockito.never()).recordActivity(any(),
                                org.mockito.ArgumentMatchers.anyInt());
        }
}
