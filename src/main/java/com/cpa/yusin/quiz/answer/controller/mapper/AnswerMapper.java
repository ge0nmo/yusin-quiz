package com.cpa.yusin.quiz.answer.controller.mapper;

import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.question.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class AnswerMapper {
    public Answer toAnswerEntity(AnswerRegisterRequest request, Question question, Member member) {
        return Answer.builder()
                .member(member)
                .content(request.getContent())
                .question(question)
                .build();
    }

    public Answer toAnswerEntity(AdminAnswerRegisterRequest request, Member member, Question question) {
        return Answer.builder()
                .member(member)
                .content(request.getContent())
                .question(question)
                .build();
    }

    public AnswerDTO toAnswerDTO(Answer answer) {
        Member member = answer.getMember();

        return AnswerDTO.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .memberId(member.getId())
                .username(member.getUsername())
                .build();
    }
}
