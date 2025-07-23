package com.cpa.yusin.quiz.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Aspect
public class LoggingAspect
{
    private final ThreadLocalLogTrace logTrace;

    public LoggingAspect(ThreadLocalLogTrace loggingTrace)
    {
        this.logTrace = loggingTrace;
    }

    @Around("within(@org.springframework.stereotype.Service *) || " +
            "within(@org.springframework.stereotype.Repository *) || " +
            "within(@org.springframework.web.bind.annotation.RestController *)")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable
    {
        TraceStatus status = null;
        try{
            String message = joinPoint.getSignature().toShortString();

            status = logTrace.begin(message);

            // 로직 호출
            Object result = joinPoint.proceed();
            logTrace.end(status);
            return result;
        } catch (Exception e){
            logTrace.exception(status, e);
            throw e;
        }
    }
}
