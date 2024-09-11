package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.subject.infrastructure.Subject;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Exam extends BaseEntity
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int maxProblemCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false, updatable = false)
    private Subject subject;

    public static Exam from(ExamDomain domain)
    {
        Exam exam = new Exam();
        exam.id = domain.getId();
        exam.name = domain.getName();
        exam.year = domain.getYear();
        exam.maxProblemCount = domain.getMaxProblemCount();
        exam.setCreatedAt(domain.getCreatedAt());
        exam.setUpdatedAt(domain.getUpdatedAt());

        exam.subject = Subject.from(domain.getSubjectDomain());

        return exam;
    }

    public ExamDomain toModel()
    {
        return ExamDomain.builder()
                .id(this.id)
                .name(this.name)
                .year(this.year)
                .maxProblemCount(this.maxProblemCount)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .subjectDomain(this.subject.toModel())
                .build();
    }
}
