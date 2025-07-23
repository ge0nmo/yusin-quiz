package com.cpa.yusin.quiz.visitor.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VisitorTest
{
    @Test
    void of()
    {
        // given
        String ip = "127.0.0.1";
        String userAgent = "Chrome/Windows";
        LocalDateTime today = LocalDateTime.of(2025, 7, 17, 0, 0, 0, 0);
        // when
        Visitor result = Visitor.of(ip, userAgent, today);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIpAddress()).isEqualTo(ip);
        assertThat(result.getUserAgent()).isEqualTo(userAgent);
        assertThat(result.getVisitedAt()).isEqualTo(LocalDateTime.of(2025, 7, 17, 0, 0, 0, 0));
    }
}