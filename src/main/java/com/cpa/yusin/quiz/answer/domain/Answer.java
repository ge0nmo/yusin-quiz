package com.cpa.yusin.quiz.answer.domain;

import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerUpdateRequest;
import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.question.domain.Question;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Getter
@Entity
public class Answer extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String username;

    @Column(nullable = false, updatable = false)
    private String password;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, updatable = false)
    private Question question;

    public void update(String content)
    {
        this.content = content;
    }

    public boolean verifyPassword(String password)
    {
        return this.password.equals(password);
    }
}
