package com.cpa.yusin.quiz.qualification.domain;

import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qualification_exam")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QualificationExam extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 64)
    @Enumerated(EnumType.STRING)
    private QualificationExamCode code;

    @Column(nullable = false, updatable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status;

    public QualificationExam(QualificationExamCode code, ContentStatus status) {
        this.code = code;
        this.name = code.getDisplayName();
        this.status = status;
    }

    public void update(ContentStatus status) {
        this.status = status;
    }
}
