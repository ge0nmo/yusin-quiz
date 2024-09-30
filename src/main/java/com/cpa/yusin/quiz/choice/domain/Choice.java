package com.cpa.yusin.quiz.choice.domain;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.problem.domain.Problem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"problem_id", "number"})
})
@NoArgsConstructor
@Getter
@Builder
public class Choice extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Boolean isAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "problem_id")
    private Problem problem;


    public void update(long problemId, ChoiceRequest request)
    {
        validateProblemId(problemId);

        this.number = request.getNumber();
        this.content = request.getContent();
        this.isAnswer = request.getIsAnswer();
    }

    public void validateProblemId(long problemId)
    {
        if(!this.problem.getId().equals(problemId)){
            throw new GlobalException(ExceptionMessage.PROBLEM_NOT_FOUND);
        }
    }
}
