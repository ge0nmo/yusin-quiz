package com.cpa.yusin.quiz.exam.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExamStatus status = ExamStatus.DRAFT;

    private boolean isRemoved;

    public ExamStatus getStatus() {
        return status == null ? ExamStatus.DRAFT : status;
    }

    public static Exam from(String name, int year, long subjectId, ExamStatus status) {
        return Exam.builder()
                .name(name)
                .year(year)
                .subjectId(subjectId)
                .status(status == null ? ExamStatus.DRAFT : status)
                .build();
    }

    public void update(String name, int year, ExamStatus status) {
        this.name = name;
        this.year = year;
        this.status = status == null ? getStatus() : status;
    }

    public void delete(long deletedMarker) {
        this.isRemoved = true;
        this.name = name + "_deleted_" + deletedMarker;
    }
}
