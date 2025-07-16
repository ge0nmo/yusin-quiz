package com.cpa.yusin.quiz.visitor.controller.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VisitorSerialization
{
    private String ipAddress;
    private String userAgent;
    private LocalDate visitedAt;

    public static VisitorSerialization from(String ipAddress, String userAgent, LocalDate visitedAt)
    {
        VisitorSerialization visitorSerialization = new VisitorSerialization();
        visitorSerialization.ipAddress = ipAddress;
        visitorSerialization.userAgent = userAgent;
        visitorSerialization.visitedAt = visitedAt;
        return visitorSerialization;
    }

}
