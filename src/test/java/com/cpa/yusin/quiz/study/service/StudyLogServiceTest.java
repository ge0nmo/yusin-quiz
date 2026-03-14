package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudyLogServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 15, 9, 0);

    @InjectMocks
    private StudyLogService studyLogService;

    @Mock
    private DailyStudyLogRepository dailyStudyLogRepository;

    @Mock
    private ClockHolder clockHolder;

    @Test
    @DisplayName("활동 기록은 유니크 키 기준 upsert 로 하루 로그를 누적한다")
    void recordActivity_shouldUseUpsertWithClockBasedDate() {
        Long memberId = 1L;
        given(clockHolder.getCurrentDateTime()).willReturn(NOW);

        studyLogService.recordActivity(memberId, 5);

        verify(dailyStudyLogRepository).upsertSolvedCount(memberId, NOW.toLocalDate(), 5, NOW);
    }

    @Test
    @DisplayName("활동 수가 0 이하면 아무 작업도 하지 않는다")
    void recordActivity_whenCountIsNotPositive_thenIgnore() {
        studyLogService.recordActivity(1L, 0);

        verify(clockHolder, never()).getCurrentDateTime();
        verify(dailyStudyLogRepository, never()).upsertSolvedCount(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("현재 연속 학습 일수는 ClockHolder 기준 날짜 경계를 사용한다")
    void calculateCurrentStreak_shouldUseClockHolderBoundary() {
        Long memberId = 1L;
        LocalDate today = NOW.toLocalDate();
        given(clockHolder.getCurrentDateTime()).willReturn(NOW);
        given(dailyStudyLogRepository.findByMemberIdAndDateBetween(memberId, today.minusDays(365), today))
                .willReturn(new ArrayList<>(List.of(
                        DailyStudyLog.createWithCount(Member.builder().id(memberId).build(), today.minusDays(1), 3),
                        DailyStudyLog.createWithCount(Member.builder().id(memberId).build(), today.minusDays(2), 2),
                        DailyStudyLog.createWithCount(Member.builder().id(memberId).build(), today.minusDays(4), 1)
                )));

        int streak = studyLogService.calculateCurrentStreak(memberId);

        assertThat(streak).isEqualTo(2);
    }

    @Test
    @DisplayName("월간 로그 조회는 전달된 YearMonth 범위를 그대로 사용한다")
    void getMonthlyLog_shouldQueryRequestedRange() {
        Long memberId = 1L;
        YearMonth yearMonth = YearMonth.of(2025, 1);

        studyLogService.getMonthlyLog(memberId, yearMonth);

        verify(dailyStudyLogRepository).findByMemberIdAndDateBetween(
                memberId,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );
    }
}
