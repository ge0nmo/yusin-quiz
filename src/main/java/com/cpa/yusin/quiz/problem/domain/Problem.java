package com.cpa.yusin.quiz.problem.domain;

import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Builder
public class Problem
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;


    public void update(long examId, ProblemRequest request)
    {
        validateExamId(examId);

        this.content = request.getContent();
        this.number = request.getNumber();
    }

    private void validateExamId(long examId)
    {
        if(!exam.getId().equals(examId))
            throw new GlobalException(ExceptionMessage.EXAM_NOT_FOUND);

    }
}
