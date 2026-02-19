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

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    @DisplayName("활동 기록 - 로그가 이미 존재하면 업데이트 수행 (Exists -> Update)")
    void recordActivity_whenExists_thenUpdate() {
        // given
        Long memberId = 1L;
        int count = 5;
        LocalDate today = LocalDate.now();
        DailyStudyLog log = mock(DailyStudyLog.class);

        given(dailyStudyLogRepository.findByMemberIdAndDate(memberId, today))
                .willReturn(Optional.of(log));

        // when
        studyLogService.recordActivity(memberId, count);

        // then
        verify(log).increaseSolvedCount(count);
        verify(dailyStudyLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("활동 기록 - 로그가 없으면 생성 후 저장 (New -> Create -> Save)")
    void recordActivity_whenNew_thenCreateAndSave() {
        // given
        Long memberId = 1L;
        int count = 5;
        LocalDate today = LocalDate.now();
        Member member = mock(Member.class);

        given(dailyStudyLogRepository.findByMemberIdAndDate(memberId, today))
                .willReturn(Optional.empty());
        given(memberRepository.getReferenceById(memberId))
                .willReturn(member);

        // when
        studyLogService.recordActivity(memberId, count);

        // then
        verify(dailyStudyLogRepository).save(any(DailyStudyLog.class));
    }
}
