package com.cpa.yusin.quiz.visitor.service;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.visitor.controller.dto.DailyVisitorCountDto;
import com.cpa.yusin.quiz.visitor.controller.dto.VisitorSerialization;
import com.cpa.yusin.quiz.visitor.domain.Visitor;
import com.cpa.yusin.quiz.visitor.service.port.VisitorRedisTemplate;
import com.cpa.yusin.quiz.visitor.service.port.VisitorRepository;
import com.cpa.yusin.quiz.visitor.service.port.VisitorSerializer;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitorServiceTest
{
    VisitorService visitorService;

    @Mock
    VisitorRepository visitorRepository;

    @Mock
    VisitorSerializer serializer;

    @Mock
    VisitorRedisTemplate visitorRedisTemplate;

    @Mock
    HttpServletRequest servletRequest;

    // 2025.01.01
    TestContainer testContainer;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
        visitorService = new VisitorService(visitorRepository, serializer,
                visitorRedisTemplate, testContainer.clockHolder);
    }

    @DisplayName("Save visitor information in redis")
    @Test
    void saveInRedis()
    {
        // given
        doNothing().when(visitorRedisTemplate).saveVisitor(any(HttpServletRequest.class));

        // when
        visitorService.saveInRedis(servletRequest);

        // then
        verify(visitorRedisTemplate).saveVisitor(servletRequest);
    }

    @DisplayName("Transfer visitor data to DB")
    @Test
    void flushRedisToDatabase()
    {
        // given
        LocalDateTime currentDateTime = testContainer.clockHolder.getCurrentDateTime();
        LocalDate today = currentDateTime.toLocalDate();

        String serializedVisitor = "{\"ipAddress\":\"127.0.0.1\",\"userAgent\":\"test\",\"visitedAt\":\"2025-07-16\"}";
        VisitorSerialization visitorDto = VisitorSerialization.from("127.0.0.1", "test", currentDateTime);
        Visitor visitor = Visitor.of("127.0.0.1", "test", currentDateTime);


        given(visitorRedisTemplate.getVisitors(today)).willReturn(Set.of(serializedVisitor));
        given(serializer.getDeserialization(serializedVisitor)).willReturn(visitorDto);
        given(visitorRepository.saveAll(any())).willReturn(List.of(visitor));

        // when

        visitorService.flushRedisToDatabase();

        // then
        verify(visitorRepository).saveAll(any());
        verify(visitorRedisTemplate).deleteVisitors(today);
    }

    @DisplayName("Does not transfer data when no key")
    @Test
    void flushRedisToDatabaseWhenNoKey()
    {
        // given
        LocalDate today = testContainer.clockHolder.getCurrentDateTime().toLocalDate();
        //given(visitorRedisTemplate.hasKey(today)).willReturn(false);

        // when
        visitorService.flushRedisToDatabase();

        // then
        verify(visitorRepository, never()).saveAll(any());
        verify(visitorRedisTemplate, never()).deleteVisitors(today);
    }

    @Test
    void getVisitorCount() {
        // given
        LocalDate start = LocalDate.of(2025, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 16);
        List<DailyVisitorCountDto> expected = List.of(
                new DailyVisitorCountDto(start, 10L),
                new DailyVisitorCountDto(end, 5L)
        );

        given(visitorRepository.countByVisitedAtBetween(start, end)).willReturn(expected);

        // when
        List<DailyVisitorCountDto> actual = visitorService.getVisitorCount(start, end);

        // then
        assertThat(actual).isEqualTo(expected);
        verify(visitorRepository).countByVisitedAtBetween(start, end);
    }

}