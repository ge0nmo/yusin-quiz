package com.cpa.yusin.quiz.visitor.service.port;

import com.cpa.yusin.quiz.visitor.controller.dto.VisitorSerialization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class VisitorSerializer
{
    private final ObjectMapper objectMapper;

    public String getSerialization(String ipAddress, String userAgent, LocalDate visitedAt)
    {
        VisitorSerialization visitor = VisitorSerialization.from(ipAddress, userAgent, visitedAt);
        try {
            return objectMapper.writeValueAsString(visitor);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public VisitorSerialization getDeserialization(String serialization)
    {
        try{
            return objectMapper.readValue(serialization, VisitorSerialization.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
