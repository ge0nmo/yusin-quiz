package com.cpa.yusin.quiz.question.controller.mapper;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionRegisterRequest;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {
    public Question toQuestionEntity(QuestionRegisterRequest request, Problem problem, Member member) {
        return Question.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .member(member)
                .problem(problem)
                .answerCount(0)
                .build();
    }

    public QuestionDTO toQuestionDTO(Question question) {
        Member member = question.getMember();

        return QuestionDTO.builder()
                .id(question.getId())
                .title(question.getTitle())
                .content(question.getContent())
                .createdAt(question.getCreatedAt())
                .answerCount(question.getAnswerCount())
                .answeredByAdmin(question.isAnsweredByAdmin())
                .problemId(question.getProblem().getId())
                .memberId(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .build();
    }
}
