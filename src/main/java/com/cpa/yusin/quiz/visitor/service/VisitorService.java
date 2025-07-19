package com.cpa.yusin.quiz.visitor.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.visitor.controller.dto.DailyVisitorCountDto;
import com.cpa.yusin.quiz.visitor.domain.Visitor;
import com.cpa.yusin.quiz.visitor.service.port.VisitorRedisTemplate;
import com.cpa.yusin.quiz.visitor.service.port.VisitorRepository;
import com.cpa.yusin.quiz.visitor.service.port.VisitorSerializer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class VisitorService
{
    private final VisitorRepository visitorRepository;
    private final VisitorSerializer serializer;
    private final VisitorRedisTemplate visitorRedisTemplate;
    private final ClockHolder clockHolder;

    public void saveInRedis(HttpServletRequest request) {
        try {
            visitorRedisTemplate.saveVisitor(request);
        } catch (Exception e) {
            log.error("Failed to save visitor in Redis", e);
            throw e;
        }
    }

    public void flushRedisToDatabase() {
        LocalDate today = clockHolder.getCurrentDateTime().toLocalDate();

        try {
            if (!visitorRedisTemplate.hasKey(today)) {
                log.info("No Redis key exists for date: {}", today);
                return;
            }

            Set<String> serializedVisitors = visitorRedisTemplate.getVisitors(today);
            if (serializedVisitors.isEmpty()) {
                log.info("No visitors to flush for date: {}", today);
                return;
            }

            List<Visitor> visitors = serializedVisitors.stream()
                    .map(serializer::getDeserialization)
                    .map(v -> Visitor.of(
                            v.getIpAddress(),
                            v.getUserAgent(),
                            v.getVisitedAt()
                    ))
                    .distinct()
                    .collect(Collectors.toList());

            if (visitors.isEmpty()) {
                log.info("No unique visitors to save for date: {}", today);
                return;
            }

            try {
                for(Visitor visitor : visitors)
                {
                    visitorRepository.save(visitor);
                }

                visitorRedisTemplate.deleteVisitors(today);
                log.info("Successfully flushed {} visitors to database for date: {}",
                        visitors.size(), today);
            } catch (Exception e) {
                log.error("Failed to save visitors to database: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to save visitors", e);
            }

        } catch (Exception e) {
            log.error("Error during Redis to Database flush: {}", e.getMessage(), e);
            throw e;
        }

    }

    @Transactional(readOnly = true)
    public List<DailyVisitorCountDto> getVisitorCount(LocalDate start, LocalDate end) {
        return visitorRepository.countByVisitedAtBetween(start, end);
    }

}
