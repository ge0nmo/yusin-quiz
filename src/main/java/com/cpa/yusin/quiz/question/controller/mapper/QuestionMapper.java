package com.cpa.yusin.quiz.question.controller.mapper;

import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionRegisterRequest;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.domain.Question;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper
{
    public Question toQuestionEntity(QuestionRegisterRequest request, Problem problem)
    {
        return Question.builder()
                .password(request.getPassword())
                .title(request.getTitle())
                .username(request.getUsername())
                .content(request.getContent())
                .problem(problem)
                .answerCount(0)
                .build();
    }

    public QuestionDTO toQuestionDTO(Question question)
    {
        return QuestionDTO.builder()
                .id(question.getId())
                .title(question.getTitle())
                .username(question.getUsername())
                .content(question.getContent())
                .createdAt(question.getCreatedAt())
                .answerCount(question.getAnswerCount())
                .answeredByAdmin(question.isAnsweredByAdmin())
                .problemId(question.getProblem().getId())
                .build();
    }
}
