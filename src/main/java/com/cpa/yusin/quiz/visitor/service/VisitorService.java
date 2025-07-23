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

    @Transactional
    public void flushRedisToDatabase()
    {
        LocalDate today = clockHolder.getCurrentDateTime().toLocalDate();

        Set<String> serializedVisitors = visitorRedisTemplate.getVisitors(today);
        if (serializedVisitors.isEmpty()) {
            log.info("No visitors to flush for date: {}", today);
            return;
        }

        List<Visitor> visitors = deserializeVisitors(serializedVisitors);

        if (visitors.isEmpty()) {
            log.info("No unique visitors to save for date: {}", today);
            return;
        }

        visitorRepository.saveAll(visitors);

        visitorRedisTemplate.deleteVisitors(today);
        visitorRedisTemplate.deleteVisitedKey();
    }


    private List<Visitor> deserializeVisitors(Set<String> serializedVisitors)
    {
        return serializedVisitors.stream()
                .map(serializer::getDeserialization)
                .map(v -> Visitor.of(
                        v.getIpAddress(),
                        v.getUserAgent(),
                        v.getVisitedAt()
                ))
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DailyVisitorCountDto> getVisitorCount(LocalDate start, LocalDate end) {
        return visitorRepository.countByVisitedAtBetween(start, end);
    }

}
