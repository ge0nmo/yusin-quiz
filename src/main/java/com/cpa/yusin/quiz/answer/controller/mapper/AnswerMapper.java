package com.cpa.yusin.quiz.answer.controller.mapper;

import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.domain.Answer;
import org.springframework.stereotype.Component;

@Component
public class AnswerMapper
{
    public AnswerDTO toAnswerDTO(Answer answer)
    {
        return AnswerDTO.builder()
                .id(answer.getId())
                .username(answer.getUsername())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .build();
    }
}
