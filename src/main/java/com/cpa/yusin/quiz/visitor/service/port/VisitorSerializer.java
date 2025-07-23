package com.cpa.yusin.quiz.visitor.service.port;

import com.cpa.yusin.quiz.visitor.controller.dto.VisitorSerialization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class VisitorSerializer
{
    private final ObjectMapper objectMapper;

    public String getSerialization(String ipAddress, String userAgent, LocalDateTime visitedAt)
    {
        VisitorSerialization visitor = VisitorSerialization.from(ipAddress, userAgent, visitedAt);
        log.info("getSerialization: ip = {}, userAgent = {}, visitedAt = {}", ipAddress, userAgent, visitedAt);
        try {
            return objectMapper.writeValueAsString(visitor);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public VisitorSerialization getDeserialization(String serialization)
    {
        try{
            VisitorSerialization visitorSerialization = objectMapper.readValue(serialization, VisitorSerialization.class);
            log.info("getDeserialization: ip = {}, userAgent = {}, visitedAt = {}", visitorSerialization.getIpAddress(), visitorSerialization.getUserAgent(), visitorSerialization.getVisitedAt());
            return visitorSerialization;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
