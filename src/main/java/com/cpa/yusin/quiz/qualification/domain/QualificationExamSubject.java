package com.cpa.yusin.quiz.qualification.domain;

import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.subject.domain.Subject;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qualification_exam_subject", uniqueConstraints =
        @UniqueConstraint(name = "uk_qualification_subject", columnNames = {"qualification_exam_id", "subject_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QualificationExamSubject extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qualification_exam_id", nullable = false)
    private QualificationExam qualificationExam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status;

    @Column(nullable = false)
    private int displayOrder;

    public QualificationExamSubject(QualificationExam qualificationExam, Subject subject,
                                    ContentStatus status, int displayOrder) {
        this.qualificationExam = qualificationExam;
        this.subject = subject;
        this.status = status;
        this.displayOrder = displayOrder;
    }

    public void update(ContentStatus status, int displayOrder) {
        this.status = status;
        this.displayOrder = displayOrder;
    }
}
