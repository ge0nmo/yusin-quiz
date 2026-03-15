package com.cpa.yusin.quiz.subject.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Subject extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubjectStatus status = SubjectStatus.PUBLISHED;

    private boolean isRemoved;

    public SubjectStatus getStatus() {
        return status == null ? SubjectStatus.PUBLISHED : status;
    }

    public void update(String name, SubjectStatus status) {
        this.name = name;
        this.status = status;
    }

    public void delete(long deletedMarker) {
        this.name = name + "_deleted_" + deletedMarker;
        this.isRemoved = true;
    }
}
