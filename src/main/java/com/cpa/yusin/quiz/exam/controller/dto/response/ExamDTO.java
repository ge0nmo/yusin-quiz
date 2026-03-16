package com.cpa.yusin.quiz.exam.controller.dto.response;

import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamDTO
{
    private final long id;
    private final String name;
    private final int year;
    private final ExamStatus status;
}
