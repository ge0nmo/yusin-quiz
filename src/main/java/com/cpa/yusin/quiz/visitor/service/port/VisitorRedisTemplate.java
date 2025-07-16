package com.cpa.yusin.quiz.visitor.service.port;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.visitor.controller.dto.VisitorSerialization;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final ClockHolder clockHolder;
    private final VisitorSerializer serializer;

    private static final String VISITOR_KEY = "ActiveUsers:";

    /**
     * saved in redis for an hour
     */

    public void saveVisitorKey(String visitorSerialization)
    {
        if(!redisTemplate.hasKey(VISITOR_KEY + visitorSerialization)){
            redisTemplate.opsForValue().set(VISITOR_KEY + visitorSerialization, visitorSerialization, Duration.ofMinutes(30));
        }
    }

    public VisitorSerialization getByIpAndUserAgent(String ipAddress, String userAgent, LocalDate today)
    {
        String serialization = serializer.getSerialization(ipAddress, userAgent, today);
        String value = redisTemplate.opsForValue().get(VISITOR_KEY + serialization);

        return serializer.getDeserialization(value);
    }

    public List<String> getVisitorValues()
    {
        Set<String> keys = redisTemplate.keys(VISITOR_KEY + "*");
        return redisTemplate.opsForValue().multiGet(keys);
    }

    public void deleteSerialization(String serialization)
    {
        redisTemplate.delete(VISITOR_KEY + serialization);
    }

}
