package com.cpa.yusin.quiz.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class PerformanceLoggingAspect {

    private final ThreadLocal<Integer> depthHolder = ThreadLocal.withInitial(() -> 0);

    // [변경 1] 보안상 위험한 패키지 및 불필요한 하위 레이어 제외
    // 1. security/auth 패키지 제외 (JWT, UserDetails 등)
    // 2. Repository 제외 (너무 시끄러움, 병목 지점 파악이 필요할 때만 별도 프로파일로 켬)
    @Pointcut("execution(* com.cpa.yusin.quiz..*Controller.*(..)) || " +
            "execution(* com.cpa.yusin.quiz..*Service.*(..))")
    public void basePackage() {}

    @Pointcut("!execution(* com.cpa.yusin.quiz.global..*(..)) && " +
            "!execution(* com.cpa.yusin.quiz..*FileService.*(..))")
    public void excludeComponents() {}

    @Around("basePackage() && excludeComponents()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        // [변경 2] 특정 민감한 메서드 이름이 포함되면 아예 인자 로깅 스킵 (방어적 코딩)
        boolean isSensitive = methodName.toLowerCase().contains("login") ||
                methodName.toLowerCase().contains("auth") ||
                methodName.toLowerCase().contains("password");

        int currentDepth = depthHolder.get();
        String prefix = "|   ".repeat(currentDepth);

        // 민감한 메서드면 args 숨김 ("HIDDEN")
        String argsString = isSensitive ? "[PROTECTED]" : LogUtils.argsToString(args);

        log.info("{}--> Call: {} | args={}", prefix, methodName, argsString);

        depthHolder.set(currentDepth + 1);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            Object result = joinPoint.proceed();

            stopWatch.stop();
            long totalTime = stopWatch.getTotalTimeMillis();

            String resultString = isSensitive ? "[PROTECTED]" : LogUtils.toSimpleString(result);

            if (totalTime > 1000) {
                log.warn("{}<-- [SLOW] Return: {} | time={}ms", prefix, methodName, totalTime);
            } else {
                log.info("{}<-- Return: {} | time={}ms | result={}",
                        prefix, methodName, totalTime, resultString);
            }

            return result;

        } catch (Exception e) {
            stopWatch.stop();
            // 에러는 중요하므로 메시지는 남기되, StackTrace는 프레임워크가 남기도록 유도하거나 필요 시 여기서 로깅
            log.error("{}<X- Exception: {} | time={}ms | message={}",
                    prefix, methodName, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        } finally {
            if (currentDepth == 0) {
                depthHolder.remove();
            } else {
                depthHolder.set(currentDepth);
            }
        }
    }
}