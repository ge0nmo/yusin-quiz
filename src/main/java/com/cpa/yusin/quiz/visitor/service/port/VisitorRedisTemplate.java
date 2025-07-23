package com.cpa.yusin.quiz.visitor.service.port;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.utils.CommonFunction;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Repository
public class VisitorRedisTemplate
{
    private static final String VISITOR_KEY = "visitors:%s";
    private static final String VISITED = "visitors:dedup-%s";

    private static final Duration KEY_TTL = Duration.ofHours(1); // 1시간 후

    private final RedisTemplate<String, String> redisTemplate;
    private final ClockHolder clockHolder;
    private final VisitorSerializer serializer;

    public void saveVisitor(HttpServletRequest request)
    {
        // 맨 처음 들어오게 되면
        // visitedUser: + ipAddress가 키에 있는지 확인
        // 없다면
        // visitors:today key에 set value로 ip address, user agent, time 저장

        String ipAddress = CommonFunction.getIpAddress(request);

        String visitedKey = String.format(VISITED, ipAddress);
        if(redisTemplate.hasKey(visitedKey)) {
            log.info("This IP address has already visited.");
            return;
        }

        LocalDateTime currentDateTime = clockHolder.getCurrentDateTime();
        LocalDate today = currentDateTime.toLocalDate();
        String userAgent = CommonFunction.getUserAgent(request);

        String key = String.format(VISITOR_KEY, today);
        String serialization = serializer.getSerialization(ipAddress, userAgent, currentDateTime);

        redisTemplate.opsForSet().add(key, serialization);
        redisTemplate.expire(key, KEY_TTL);

        redisTemplate.opsForSet().add(visitedKey, "true");
        redisTemplate.expire(visitedKey, KEY_TTL);
    }

    public Set<String> getVisitors(LocalDate date) {
        String key = String.format(VISITOR_KEY, date);
        return redisTemplate.opsForSet().members(key);
    }

    public void deleteVisitors(LocalDate date) {
        String key = String.format(VISITOR_KEY, date);
        redisTemplate.delete(key);
    }

    public Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisConnection connection) -> {
            Set<String> matchingKeys = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(1000).build())) {
                while (cursor.hasNext()) {
                    matchingKeys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to scan Redis keys", e);
            }
            return matchingKeys;
        });
    }


    public void deleteVisitedKey()
    {
        String visitedKey = String.format(VISITED, "*");
        Set<String> keysToDelete = scanKeys(visitedKey);
        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }

    }

    public Boolean hasKey(LocalDate date)
    {
        String key = String.format(VISITOR_KEY, date);
        return redisTemplate.hasKey(key);
    }

}
