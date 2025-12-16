package com.cpa.yusin.quiz.subject.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
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
public class Subject extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private boolean isRemoved;

    public void update(String name)
    {
        this.name = name;
    }

    public void delete()
    {
        this.name = name + "_deleted_" + System.currentTimeMillis();
        this.isRemoved = true;
    }
}
