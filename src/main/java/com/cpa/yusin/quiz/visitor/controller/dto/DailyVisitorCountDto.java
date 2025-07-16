package com.cpa.yusin.quiz.visitor.controller.dto;

import java.time.LocalDate;

public record DailyVisitorCountDto(LocalDate date, long count)
{
}
