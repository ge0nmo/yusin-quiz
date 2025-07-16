package com.cpa.yusin.quiz.visitor.service.port;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.utils.CommonFunction;
import com.cpa.yusin.quiz.visitor.controller.dto.VisitorSerialization;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class VisitorRedisTemplate
{
    private static final String VISITOR_KEY = "visitors:%s";
    private static final Duration KEY_TTL = Duration.ofDays(2); // 2일 후

    private final RedisTemplate<String, String> redisTemplate;
    private final ClockHolder clockHolder;
    private final VisitorSerializer serializer;


    public void saveVisitor(HttpServletRequest request)
    {
        LocalDate today = clockHolder.getCurrentDateTime().toLocalDate();
        String key = String.format(VISITOR_KEY, today);

        String ipAddress = CommonFunction.getIpAddress(request);
        String userAgent = CommonFunction.getUserAgent(request);
        String serializedVisitor = serializer.getSerialization(ipAddress, userAgent, today);

        redisTemplate.opsForSet().add(key, serializedVisitor);
        redisTemplate.expire(key, KEY_TTL); // TTL 설정
    }

    public Set<String> getVisitors(LocalDate date) {
        String key = String.format(VISITOR_KEY, date);
        return redisTemplate.opsForSet().members(key);
    }

    public void deleteVisitors(LocalDate date) {
        String key = String.format(VISITOR_KEY, date);
        redisTemplate.delete(key);
    }

    public Long getVisitorCount(LocalDate date)
    {
        String key = String.format(VISITOR_KEY, date);
        return redisTemplate.opsForSet().size(key);
    }

    public Boolean hasKey(LocalDate date)
    {
        String key = String.format(VISITOR_KEY, date);
        return redisTemplate.hasKey(key);
    }

    public Duration getExpire(LocalDate date)
    {
        String key = String.format(VISITOR_KEY, date);
        Long seconds = redisTemplate.getExpire(key);
        return Duration.ofSeconds(seconds);
    }


}
