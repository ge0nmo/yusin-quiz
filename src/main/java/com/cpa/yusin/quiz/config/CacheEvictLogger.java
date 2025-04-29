package com.cpa.yusin.quiz.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class CacheEvictLogger
{
    @Before("@annotation(cacheEvict)")
    public void logCacheEvict(JoinPoint joinPoint, CacheEvict cacheEvict) {
        String method = joinPoint.getSignature().toShortString();
        log.info("🧹 [@CacheEvict] method: {}, cache: {}, key: {}",
                method, cacheEvict.value(), cacheEvict.key());
    }
}
