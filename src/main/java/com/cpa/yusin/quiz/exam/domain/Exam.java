package com.cpa.yusin.quiz.exam.domain;

import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam", uniqueConstraints =
        @UniqueConstraint(name = "uk_exam_qualification_year_name", columnNames = {"qualification_exam_id", "exam_year", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exam extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qualification_exam_id", nullable = false)
    private QualificationExam qualificationExam;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "exam_year", nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status;

    public Exam(QualificationExam qualificationExam, String name, int year, ContentStatus status) {
        this.qualificationExam = qualificationExam;
        this.name = name;
        this.year = year;
        this.status = status;
    }

    public void update(QualificationExam qualificationExam, String name, int year, ContentStatus status) {
        this.qualificationExam = qualificationExam;
        this.name = name;
        this.year = year;
        this.status = status;
    }
}
