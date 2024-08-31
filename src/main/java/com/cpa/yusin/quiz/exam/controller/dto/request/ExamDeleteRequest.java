package com.cpa.yusin.quiz.exam.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ExamDeleteRequest
{
    List<Long> examIds;
}
