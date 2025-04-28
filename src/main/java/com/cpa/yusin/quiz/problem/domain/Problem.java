package com.cpa.yusin.quiz.problem.domain;

import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.exception.ExamException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
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

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private int number;

    @Column(columnDefinition = "LONGTEXT")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    public static Problem fromSaveOrUpdate(ProblemRequest request, Exam exam)
    {
        return Problem.builder()
                .content(request.getContent())
                .explanation(request.getExplanation())
                .number(request.getNumber())
                .exam(exam)
                .build();
    }


    public void update(String content, int number, String explanation)
    {
        this.content = content;
        this.number = number;
        this.explanation = explanation;
    }

    private void validateExamId(long examId)
    {
        if(!exam.getId().equals(examId))
            throw new ExamException(ExceptionMessage.EXAM_NOT_FOUND);

    }
}
