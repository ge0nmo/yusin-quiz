package com.cpa.yusin.quiz.global.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TraceStatus
{
    private TraceId traceId;
    private Long startTimeMs;
    private String message;
}
