package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.domain.*;
import com.cpa.yusin.quiz.study.service.port.StudySessionRepository;
import com.cpa.yusin.quiz.study.service.port.SubmittedAnswerRepository;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
        private StudyLogService studyLogService;

        @Mock
        private ChoiceRepository choiceRepository;

        @Mock
        private Member member;

        @Test
        @DisplayName("세션 시작 - 이미 진행중인 세션이 있으면 반환 (이어풀기)")
        void startSession_whenExists_thenReturnExisting() {
                // given
                Long examId = 100L;
                ExamMode mode = ExamMode.EXAM;
                StudySession existingSession = StudySession.builder().id(1L).build();

                given(member.getId()).willReturn(1L);
                given(studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(
                                1L, examId, StudySessionStatus.IN_PROGRESS, mode))
                                .willReturn(Optional.of(existingSession));

                // when
                StudySession result = studySessionService.startSession(member, examId, mode);

                // then
                assertThat(result).isEqualTo(existingSession);
                // save 호출 안됨 (새로 생성 안함)
                verify(studySessionRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("세션 시작 - 없으면 새로 생성")
        void startSession_whenNotExists_thenCreateNew() {
                // given
                Long examId = 100L;
                ExamMode mode = ExamMode.EXAM;
                StudySession newSession = StudySession.builder().id(2L).build();

                given(member.getId()).willReturn(1L);
                given(studySessionRepository.findByMemberIdAndExamIdAndStatusAndMode(
                                1L, examId, StudySessionStatus.IN_PROGRESS, mode))
                                .willReturn(Optional.empty());

                given(studySessionRepository.save(any(StudySession.class))).willReturn(newSession);

                // when
                StudySession result = studySessionService.startSession(member, examId, mode);

                // then
                assertThat(result).isEqualTo(newSession);
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
                given(studySessionRepository.findById(sessionId)).willReturn(Optional.of(session));

                // Choice Mock
                Choice choice = Choice.builder()
                                .id(choiceId).isAnswer(correct).build();
                given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));

                given(submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId, problemId))
                                .willReturn(Optional.empty());

                // when
                ExamAnswerResponse response = studySessionService.saveAnswer(sessionId, problemId, choiceId, index);

                // then
                // 1. Check Response
                assertThat(response.isSuccess()).isTrue();
                // Exam Mode -> isCorrect null
                assertThat(response.getIsCorrect()).isNull();

                // 2. Session Index 업데이트 Check
                assertThat(session.getLastIndex()).isEqualTo(index);
                // 3. Insert 호출 Check
                verify(submittedAnswerRepository).save(any(SubmittedAnswer.class));

                // Optimization Check: Exam mode should NOT record activity here
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
                                .member(member).build(); // Needs member for log recording

                given(studySessionRepository.findById(sessionId)).willReturn(Optional.of(session));

                // Choice & Problem Mock for Explanation
                Problem problem = Problem.builder()
                                .explanation("Test Explanation").build();

                Choice choice = Choice.builder()
                                .id(choiceId).isAnswer(correct).problem(problem).build();

                given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));

                // when
                ExamAnswerResponse response = studySessionService.saveAnswer(sessionId, problemId, choiceId, 1);

                // then
                assertThat(response.getIsCorrect()).isFalse();
                assertThat(response.getExplanation()).isEqualTo("Test Explanation");
                verify(studyLogService).recordActivity(member);
        }

        @Test
        @DisplayName("답안 저장 - 수정 (Update)")
        void saveAnswer_whenExists_thenUpdate() {
                // given
                Long sessionId = 1L;
                Long problemId = 10L;
                Long choiceId = 400L; // New choice ID
                boolean correct = false;
                int index = 6;

                StudySession session = StudySession.builder().id(sessionId).mode(ExamMode.EXAM).build();
                SubmittedAnswer existingAnswer = SubmittedAnswer.builder()
                                .id(50L).choiceId(100L).isCorrect(true).build();

                given(studySessionRepository.findById(sessionId)).willReturn(Optional.of(session));

                // Choice Mock
                Choice choice = Choice.builder()
                                .id(choiceId).isAnswer(correct).build();
                given(choiceRepository.findById(choiceId)).willReturn(Optional.of(choice));

                given(submittedAnswerRepository.findByStudySessionIdAndProblemId(sessionId, problemId))
                                .willReturn(Optional.of(existingAnswer));

                // when
                studySessionService.saveAnswer(sessionId, problemId, choiceId, index);

                // then
                assertThat(session.getLastIndex()).isEqualTo(index);
                assertThat(existingAnswer.getChoiceId()).isEqualTo(choiceId); // Value updated
                assertThat(existingAnswer.isCorrect()).isEqualTo(correct);
                // Save 호출 안함 (Dirty Checking) - But RepositoryImpl implementation might vary.
                // Logic says "updateAnswer" method on Entity. JPA dirty check handles it.
                // verify(submittedAnswerRepository).save(...) -> No, strictly speaking we don't
                // call save on update in service (relying on transaction).
        }

        @Test
        @DisplayName("시험 종료 - 점수 계산 및 잔디 기록 (실전 모드)")
        void completeSession_exam_shouldCalculateScoreAndLog() {
                // given
                Long sessionId = 1L;
                StudySession session = StudySession.builder().id(sessionId).mode(ExamMode.EXAM).member(member).build();

                // 2 Correct Answers
                List<SubmittedAnswer> answers = List.of(
                                SubmittedAnswer.builder().isCorrect(true).build(),
                                SubmittedAnswer.builder().isCorrect(true).build(),
                                SubmittedAnswer.builder().isCorrect(false).build());

                given(studySessionRepository.findById(sessionId)).willReturn(Optional.of(session));
                given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(answers);

                // when
                int finalScore = studySessionService.completeSession(sessionId);

                // then
                assertThat(finalScore).isEqualTo(10); // 2 * 5
                // Optimization Check: recordActivity called with count 3 (size of answers)
                verify(studyLogService).recordActivity(member, 3);
                assertThat(session.getStatus()).isEqualTo(StudySessionStatus.COMPLETED);
        }

        @Test
        @DisplayName("시험 종료 - 연습 모드는 잔디 기록 스킵 (이미 건별 기록됨)")
        void completeSession_practice_shouldNotLog() {
                // given
                Long sessionId = 1L;
                StudySession session = StudySession.builder().id(sessionId).mode(ExamMode.PRACTICE).member(member)
                                .build();

                given(studySessionRepository.findById(sessionId)).willReturn(Optional.of(session));
                given(submittedAnswerRepository.findAllByStudySessionId(sessionId)).willReturn(List.of()); // Answers
                                                                                                           // don't
                                                                                                           // matter for
                                                                                                           // this check

                // when
                studySessionService.completeSession(sessionId);

                // then
                // Optimization Check: recordActivity SHOULD NOT be called
                verify(studyLogService, org.mockito.Mockito.never()).recordActivity(any());
                verify(studyLogService, org.mockito.Mockito.never()).recordActivity(any(),
                                org.mockito.ArgumentMatchers.anyInt());
        }
}
