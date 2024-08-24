package com.cpa.yusin.quiz.choice.infrastructure;

import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.problem.infrastructure.Problem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"problem_id", "number"})
})
@Getter
@Entity
public class Choice extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private boolean isAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "problem_id")
    private Problem problem;

    public static Choice from(ChoiceDomain domain)
    {
        Choice choice = new Choice();
        choice.id = domain.getId();
        choice.content = domain.getContent();
        choice.number = domain.getNumber();
        choice.problem = Problem.from(domain.getProblem());
        choice.isAnswer = domain.isAnswer();
        return choice;
    }

    public ChoiceDomain toModel()
    {
        return ChoiceDomain.builder()
                .id(this.id)
                .content(this.content)
                .number(this.number)
                .isAnswer(this.isAnswer)
                .problem(this.problem.toModel())
                .build();
    }
}
