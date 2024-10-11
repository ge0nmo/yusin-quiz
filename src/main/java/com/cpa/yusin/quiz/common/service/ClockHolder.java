package com.cpa.yusin.quiz.common.service;

import java.time.LocalDateTime;

public interface ClockHolder
{
    long getCurrentTime();

    LocalDateTime getCurrentDateTime();
}
