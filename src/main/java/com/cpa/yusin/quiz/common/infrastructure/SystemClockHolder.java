package com.cpa.yusin.quiz.common.infrastructure;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SystemClockHolder implements ClockHolder
{
    @Override
    public long getCurrentTime()
    {
        return System.currentTimeMillis();
    }

    @Override
    public LocalDateTime getCurrentDateTime()
    {
        return LocalDateTime.now();
    }
}
