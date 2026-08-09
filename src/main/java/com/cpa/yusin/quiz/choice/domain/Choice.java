package com.cpa.yusin.quiz.choice.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.global.converter.JsonBlockListConverter;
import com.cpa.yusin.quiz.problem.domain.Problem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "problem_choice", uniqueConstraints =
        @UniqueConstraint(name = "uk_choice_problem_number", columnNames = {"problem_id", "number"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Choice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean answer;

    @Convert(converter = JsonBlockListConverter.class)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private List<Map<String, Object>> explanation = new ArrayList<>();

    public Choice(int number, String content, boolean answer, List<Map<String, Object>> explanation) {
        this.number = number;
        this.content = content;
        this.answer = answer;
        this.explanation = new ArrayList<>(explanation == null ? List.of() : explanation);
    }

    public void attachTo(Problem problem) {
        this.problem = problem;
    }

    public void update(String content, boolean answer, List<Map<String, Object>> explanation) {
        this.content = content;
        this.answer = answer;
        this.explanation = new ArrayList<>(explanation == null ? List.of() : explanation);
    }
}
