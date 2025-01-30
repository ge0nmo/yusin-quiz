package com.cpa.yusin.quiz.question.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.problem.domain.Problem;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Getter
@Entity
public class Question extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;


    public void update(String title, String content)
    {
        this.title = title;
        this.content = content;
    }
}
