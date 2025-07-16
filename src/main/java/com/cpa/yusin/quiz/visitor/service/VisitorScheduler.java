package com.cpa.yusin.quiz.visitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class VisitorScheduler
{
    private final VisitorService visitorService;

    @Scheduled(initialDelay = 30 * 60 * 1000, fixedDelay = 30 * 60 * 1000)
    public void callVisitorData()
    {
        visitorService.flushRedisToDatabase();
    }
}
