package com.cpa.yusin.quiz.visitor.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.utils.CommonFunction;
import com.cpa.yusin.quiz.visitor.controller.dto.VisitorSerialization;
import com.cpa.yusin.quiz.visitor.domain.Visitor;
import com.cpa.yusin.quiz.visitor.service.port.VisitorRedisTemplate;
import com.cpa.yusin.quiz.visitor.service.port.VisitorRepository;
import com.cpa.yusin.quiz.visitor.service.port.VisitorSerializer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VisitorService
{
    private final VisitorRepository visitorRepository;
    private final VisitorSerializer serializer;
    private final VisitorRedisTemplate visitorRedisTemplate;
    private final ClockHolder clockHolder;

    public void saveInRedis(HttpServletRequest request)
    {
        String ipAddress = CommonFunction.getIpAddress(request);
        String userAgent = CommonFunction.getUserAgent(request);
        LocalDate today = clockHolder.getCurrentDateTime().toLocalDate();

        String visitorSerialization = serializer.getSerialization(ipAddress, userAgent, today);

        visitorRedisTemplate.saveVisitorKey(visitorSerialization);
    }

    private void saveAllInDb(List<String> values)
    {
        List<Visitor> visitors = new ArrayList<>();
        for (String value : values) {
            VisitorSerialization visitorSerialization = serializer.getDeserialization(value);
            Visitor visitor = Visitor.of(visitorSerialization.getIpAddress(),
                    visitorSerialization.getUserAgent(), visitorSerialization.getVisitedAt());
            visitors.add(visitor);

            visitorRedisTemplate.deleteSerialization(value);
        }
        visitorRepository.saveAll(visitors);
    }

    @Transactional
    public void flushRedisToDatabase()
    {
        List<String> visitorValues = visitorRedisTemplate.getVisitorValues();
        saveAllInDb(visitorValues);
    }

}
