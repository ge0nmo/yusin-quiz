package com.cpa.yusin.quiz.exam.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor()
@Entity
@Getter
public class Exam extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false, updatable = false)
    private Long subjectId;

    public static Exam from(ExamCreateRequest request, long subjectId)
    {
        return Exam.builder()
                .name(request.getName())
                .year(request.getYear())
                .subjectId(subjectId)
                .build();
    }

    public void update(ExamUpdateRequest request)
    {
        this.name = request.getName();
        this.year = request.getYear();
    }
}
