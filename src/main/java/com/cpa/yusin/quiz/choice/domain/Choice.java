package com.cpa.yusin.quiz.choice.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.global.converter.BlockListConverter;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Column(columnDefinition = "json")
    @Convert(converter = BlockListConverter.class)
    private List<Block> explanationJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "problem_id")
    private Problem problem;

    public void update(int number, String content, boolean isAnswer) {
        this.number = number;
        this.content = content;
        this.isAnswer = isAnswer;
    }

    public void update(int number, String content, boolean isAnswer, List<Block> explanationJson) {
        this.number = number;
        this.content = content;
        this.isAnswer = isAnswer;
        this.explanationJson = explanationJson != null ? explanationJson : new ArrayList<>();
    }

    public static Choice fromSaveOrUpdate(String content, int number, Boolean isAnswer, Problem problem) {
        return Choice.builder()
                .content(content)
                .number(number)
                .isAnswer(isAnswer)
                .problem(problem)
                .explanationJson(new ArrayList<>())
                .build();
    }

    public static Choice fromSaveOrUpdate(String content, int number, Boolean isAnswer, List<Block> explanationJson, Problem problem) {
        return Choice.builder()
                .content(content)
                .number(number)
                .isAnswer(isAnswer)
                .explanationJson(explanationJson != null ? explanationJson : new ArrayList<>())
                .problem(problem)
                .build();
    }

    public List<Block> getExplanationJson() {
        if (this.explanationJson == null) {
            return new ArrayList<>();
        }
        return this.explanationJson;
    }

}
