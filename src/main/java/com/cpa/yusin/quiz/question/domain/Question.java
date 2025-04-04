package com.cpa.yusin.quiz.question.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.QuestionException;
import com.cpa.yusin.quiz.problem.domain.Problem;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @Column(nullable = false, updatable = false)
    private String username;

    @Column(nullable = false, updatable = false)
    private String password;

    @Column(nullable = false)
    private String title;

    private String content;

    @Column(nullable = false)
    private boolean answeredByAdmin;

    @Column(nullable = false)
    private Integer answerCount;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;


    public void update(String title, String content)
    {
        this.title = title;
        this.content = content;
    }

    public void answerByAdmin()
    {
        this.answeredByAdmin = true;
        updateAnswerCount(1);
    }

    public void updateAnswerCount(int count)
    {
        int newCount = this.answerCount + count;
        this.answerCount = Math.max(newCount, 0);
    }

    public boolean verify(String inputPassword)
    {
        return this.password.equals(inputPassword);
    }
}
