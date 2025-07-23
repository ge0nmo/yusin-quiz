package com.cpa.yusin.quiz.visitor.service.port;

import com.cpa.yusin.quiz.config.TestContainer;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VisitorRedisTemplateTest
{
    @Mock
    RedisTemplate<String, String> redisTemplate;

    TestContainer testContainer;

    @Mock
    VisitorSerializer visitorSerializer;

    @Mock
    SetOperations<String, String> setOperations;

    @Mock
    HttpServletRequest servletRequest;

    VisitorRedisTemplate visitorRedisTemplate;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
        visitorRedisTemplate = new VisitorRedisTemplate(redisTemplate, testContainer.clockHolder, visitorSerializer);

    }

    @Test
    void saveVisitor()
    {
        // given
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        LocalDate today = testContainer.clockHolder.getCurrentDateTime().toLocalDate();
        String key = String.format("visitors:%s", today);
        String serializedVisitor = "{\"ipAddress\":\"127.0.0.1\",\"userAgent\":\"test\",\"visitedAt\":\"2025-01-01 00:00:00.000000\"}";

        given(visitorSerializer.getSerialization(any(), any(), any())).willReturn(serializedVisitor);

        // when
        visitorRedisTemplate.saveVisitor(servletRequest);

        // then
        verify(setOperations).add(key, serializedVisitor);
        verify(redisTemplate).expire(key, Duration.ofHours(1));
    }

    @Test
    void getVisitors()
    {
        // given
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        LocalDate today = testContainer.clockHolder.getCurrentDateTime().toLocalDate();
        String key = String.format("visitors:%s", today);
        Set<String> expected = Set.of("visitor1", "visitor2");
        given(setOperations.members(key)).willReturn(expected);

        // when
        Set<String> actual = visitorRedisTemplate.getVisitors(today);

        // then
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void deleteVisitors()
    {
        // given
        LocalDate today = testContainer.clockHolder.getCurrentDateTime().toLocalDate();
        String key = String.format("visitors:%s", today);

        // when
        visitorRedisTemplate.deleteVisitors(today);

        // then
        verify(redisTemplate).delete(key);

    }

}