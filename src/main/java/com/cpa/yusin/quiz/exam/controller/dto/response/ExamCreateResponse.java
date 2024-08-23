package com.cpa.yusin.quiz.exam.controller.dto.response;

import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamCreateResponse
{
    private final long id;
    private final String name;
    private final int year;
    private final SubjectDTO subject;
}
