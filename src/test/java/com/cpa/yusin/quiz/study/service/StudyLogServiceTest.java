package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyLogServiceTest {

    @InjectMocks
    private StudyLogService studyLogService;

    @Mock
    private DailyStudyLogRepository dailyStudyLogRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private DailyStudyLogManager dailyStudyLogManager;

    @Test
    @DisplayName("활동 기록 - 로그가 이미 존재하면 업데이트만 수행 (Update Success)")
    void recordActivity_whenExists_thenUpdate() {
        // given
        Long memberId = 1L;
        int count = 5;
        LocalDate today = LocalDate.now();

        given(dailyStudyLogRepository.increaseSolvedCount(eq(memberId), eq(today), eq(count)))
                .willReturn(1); // Update succeeded

        // when
        studyLogService.recordActivity(memberId, count);

        // then
        verify(dailyStudyLogRepository).increaseSolvedCount(eq(memberId), eq(today), eq(count));
        verify(dailyStudyLogManager, never()).createLog(any(), any(), anyInt());
    }

    @Test
    @DisplayName("활동 기록 - 로그가 없으면 생성 시도 (Update Fail -> Create Success)")
    void recordActivity_whenNew_thenCreate() {
        // given
        Long memberId = 1L;
        int count = 5;
        LocalDate today = LocalDate.now();

        // 1. First update fails
        given(dailyStudyLogRepository.increaseSolvedCount(eq(memberId), eq(today), eq(count)))
                .willReturn(0);

        // 2. Create succeeds
        doNothing().when(dailyStudyLogManager).createLog(eq(memberId), eq(today), eq(count));

        // when
        studyLogService.recordActivity(memberId, count);

        // then
        verify(dailyStudyLogRepository).increaseSolvedCount(eq(memberId), eq(today), eq(count));
        verify(dailyStudyLogManager).createLog(eq(memberId), eq(today), eq(count));
    }

    @Test
    @DisplayName("활동 기록 - 생성 중 경합 발생 시 업데이트 재시도 (Update Fail -> Create Fail -> Retry Update)")
    void recordActivity_whenRaceCondition_thenRetryUpdate() {
        // given
        Long memberId = 1L;
        int count = 5;
        LocalDate today = LocalDate.now();

        // 1. First update fails (row didn't exist)
        given(dailyStudyLogRepository.increaseSolvedCount(eq(memberId), eq(today), eq(count)))
                .willReturn(0)
                .willReturn(1); // 3. Second update succeeds (after race)

        // 2. Create fails with DataIntegrityViolationException (Race condition)
        doThrow(DataIntegrityViolationException.class).when(dailyStudyLogManager).createLog(eq(memberId), eq(today),
                eq(count));

        // when
        studyLogService.recordActivity(memberId, count);

        // then
        // Verify update called twice
        verify(dailyStudyLogRepository, times(2)).increaseSolvedCount(eq(memberId), eq(today), eq(count));
        // Verify create called once
        verify(dailyStudyLogManager).createLog(eq(memberId), eq(today), eq(count));
    }
}
