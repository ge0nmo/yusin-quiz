package com.cpa.yusin.quiz.choice.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.problem.domain.Problem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "problem_id", "number" })
})
@NoArgsConstructor
@Getter
@Builder
public class Choice extends BaseEntity {
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

    public void update(int number, String content, boolean isAnswer) {
        this.number = number;
        this.content = content;
        this.isAnswer = isAnswer;
    }

    public static Choice fromSaveOrUpdate(String content, int number, Boolean isAnswer, Problem problem) {
        return Choice.builder()
                .content(content)
                .number(number)
                .isAnswer(isAnswer)
                .problem(problem)
                .build();
    }

}
