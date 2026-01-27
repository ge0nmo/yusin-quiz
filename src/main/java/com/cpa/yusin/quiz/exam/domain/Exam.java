package com.cpa.yusin.quiz.exam.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;

import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
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
public class Exam extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false, updatable = false)
    private Long subjectId;

    private boolean isRemoved;

    public static Exam from(String name, int year, long subjectId) {
        return Exam.builder()
                .name(name)
                .year(year)
                .subjectId(subjectId)
                .build();
    }

    public void update(String name, int year) {
        this.name = name;
        this.year = year;
    }

    public void delete() {
        this.isRemoved = true;
        this.name = name + "_deleted_" + System.currentTimeMillis();
    }
}
