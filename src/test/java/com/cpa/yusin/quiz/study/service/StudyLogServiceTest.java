package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.member.domain.Member;
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
    private Member member;

    @Test
    @DisplayName("활동 기록 - 이미 기록이 있는 경우(오늘) 아무것도 하지 않음")
    void recordActivity_whenExists_thenDoNothing() {
        // given
        given(member.getId()).willReturn(1L);
        // 이미 존재함
        given(dailyStudyLogRepository.findByMemberIdAndDate(eq(1L), any(LocalDate.class)))
                .willReturn(java.util.Optional.of(DailyStudyLog.createFirst(member, LocalDate.now())));

        // when
        studyLogService.recordActivity(member);

        // then
        // save 호출되지 않음
        verify(dailyStudyLogRepository, never()).save(any());
        // increment 호출되지 않음 (로직 변경됨)
        verify(dailyStudyLogRepository, never()).increaseSolvedCount(any(), any());
    }

    @Test
    @DisplayName("활동 기록 - 기록이 없는 경우 새로 생성")
    void recordActivity_whenNotExists_thenSaveNew() {
        // given
        given(member.getId()).willReturn(1L);
        // 존재하지 않음
        given(dailyStudyLogRepository.findByMemberIdAndDate(eq(1L), any(LocalDate.class)))
                .willReturn(java.util.Optional.empty());

        // when
        studyLogService.recordActivity(member);

        // then
        verify(dailyStudyLogRepository, times(1)).save(any(DailyStudyLog.class));
    }

    @Test
    @DisplayName("활동 기록 - 동시성 이슈로 생성 실패 시(이미 생성됨) 예외 무시")
    void recordActivity_whenRaceCondition_thenIgnore() {
        // given
        given(member.getId()).willReturn(1L);
        // 1. 조회 시 없음
        given(dailyStudyLogRepository.findByMemberIdAndDate(eq(1L), any(LocalDate.class)))
                .willReturn(java.util.Optional.empty());

        // 2. 저장 시 이미 존재한다고 예외 발생
        doThrow(DataIntegrityViolationException.class)
                .when(dailyStudyLogRepository).save(any(DailyStudyLog.class));

        // when
        studyLogService.recordActivity(member);

        // then
        // save는 시도했으나 예외는 잡혀서 전파되지 않음
        verify(dailyStudyLogRepository, times(1)).save(any(DailyStudyLog.class));
    }

    @Test
    @DisplayName("월별 로그 조회")
    void getMonthlyLog() {
        // given
        YearMonth yearMonth = YearMonth.of(2023, 10);
        given(member.getId()).willReturn(1L);
        given(dailyStudyLogRepository.findByMemberIdAndDateBetween(eq(1L), any(), any()))
                .willReturn(List.of(DailyStudyLog.create(member, LocalDate.now())));

        // when
        List<DailyStudyLog> result = studyLogService.getMonthlyLog(member, yearMonth);

        // then
        assertThat(result).hasSize(1);
    }
}
