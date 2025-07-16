package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.common.service.ClockHolder;

import java.time.LocalDateTime;

public class FakeClockHolder implements ClockHolder
{
    @Override
    public long getCurrentTime()
    {
        return 100000000;
    }

    @Override
    public LocalDateTime getCurrentDateTime()
    {
        return LocalDateTime.of(2025, 1, 1, 0, 0, 0);
    }
}
