package com.cpa.yusin.quiz.common.infrastructure;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import org.springframework.stereotype.Component;

@Component
public class SystemClockHolder implements ClockHolder
{
    @Override
    public long getCurrentTime()
    {
        return System.currentTimeMillis();
    }
}
