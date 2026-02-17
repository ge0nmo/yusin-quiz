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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private Member member;

    @Test
    @DisplayName("활동 기록 - 이미 기록이 있는 경우 (Atomic Update 성공)")
    void recordActivity_whenExists_thenIncrement() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        // Update returns 1 (Success)
        given(dailyStudyLogRepository.increaseSolvedCount(eq(memberId), eq(today), eq(1)))
                .willReturn(1);

        // when
        studyLogService.recordActivity(memberId);

        // then
        verify(dailyStudyLogRepository).increaseSolvedCount(memberId, today, 1);
        verify(dailyStudyLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("활동 기록 - 기록이 없는 경우 새로 생성 (Insert)")
    void recordActivity_whenNotExists_thenSaveNew() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        // Update returns 0 (Fail)
        given(dailyStudyLogRepository.increaseSolvedCount(eq(memberId), eq(today), eq(1)))
                .willReturn(0);

        given(memberRepository.getReferenceById(memberId)).willReturn(member);

        // when
        studyLogService.recordActivity(memberId);

        // then
        verify(dailyStudyLogRepository).increaseSolvedCount(memberId, today, 1);
        verify(memberRepository).getReferenceById(memberId);
        verify(dailyStudyLogRepository).save(any(DailyStudyLog.class));
    }

    @Test
    @DisplayName("활동 기록 - 동시성 이슈로 생성 실패 시 (Race Condition) -> 재시도")
    void recordActivity_whenRaceCondition_thenRetryIncrement() {
        // given
        Long memberId = 1L;
        LocalDate today = LocalDate.now();

        // 1. First Update returns 0
        given(dailyStudyLogRepository.increaseSolvedCount(eq(memberId), eq(today), eq(1)))
                .willReturn(0);

        given(memberRepository.getReferenceById(memberId)).willReturn(member);

        // 2. Save throws Exception (Someone inserted just before us)
        doThrow(DataIntegrityViolationException.class)
                .when(dailyStudyLogRepository).save(any(DailyStudyLog.class));

        // when
        studyLogService.recordActivity(memberId);

        // then
        // Verify retry: increaseSolvedCount called twice (or at least more than once,
        // effectively logic calls it again manually? No, code explicitly calls it again
        // in catch block)
        // Actually mockito verify checks total invocations.
        // 1st call: initial try. 2nd call: catch block.
        verify(dailyStudyLogRepository, times(2)).increaseSolvedCount(memberId, today, 1);
    }

    @Test
    @DisplayName("월별 로그 조회")
    void getMonthlyLog() {
        // given
        YearMonth yearMonth = YearMonth.of(2023, 10);
        Long memberId = 1L;
        given(dailyStudyLogRepository.findByMemberIdAndDateBetween(eq(memberId), any(), any()))
                .willReturn(List.of(DailyStudyLog.createFirst(member, LocalDate.now())));

        // when
        List<DailyStudyLog> result = studyLogService.getMonthlyLog(memberId, yearMonth);

        // then
        assertThat(result).hasSize(1);
    }
}
