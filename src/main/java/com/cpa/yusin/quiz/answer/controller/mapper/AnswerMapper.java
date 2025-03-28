package com.cpa.yusin.quiz.answer.controller.mapper;

import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.web.dto.AdminAnswerRegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class AnswerMapper
{
    public Answer toAnswerEntity(AnswerRegisterRequest request, Question question)
    {
        return Answer.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .content(request.getContent())
                .question(question)
                .build();
    }

    public Answer toAnswerEntity(AdminAnswerRegisterRequest request, Member member, Question question)
    {
        return Answer.builder()
                .username(member.getUsername())
                .password(member.getUsername())
                .content(request.getContent())
                .question(question)
                .build();
    }

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
