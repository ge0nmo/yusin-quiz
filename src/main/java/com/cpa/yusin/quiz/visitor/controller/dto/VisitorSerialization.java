package com.cpa.yusin.quiz.visitor.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VisitorSerialization
{
    private String ipAddress;
    private String userAgent;
    private LocalDateTime visitedAt;

    public static VisitorSerialization from(String ipAddress, String userAgent, LocalDateTime visitedAt)
    {
        VisitorSerialization visitorSerialization = new VisitorSerialization();
        visitorSerialization.ipAddress = ipAddress;
        visitorSerialization.userAgent = userAgent;
        visitorSerialization.visitedAt = visitedAt;
        return visitorSerialization;
    }

}
