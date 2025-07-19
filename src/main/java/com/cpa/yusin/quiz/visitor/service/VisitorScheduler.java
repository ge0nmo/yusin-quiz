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

    @Scheduled(initialDelay = 60 * 1000, fixedRate = 60 * 1000)
    public void flushVisitorData() {
        try {
            log.info("Starting scheduled visitor data flush");
            visitorService.flushRedisToDatabase();
            log.info("Completed scheduled visitor data flush");
        } catch (Exception e) {
            log.error("Failed to execute scheduled visitor data flush", e);
        }
    }

}
