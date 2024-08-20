package com.cpa.yusin.quiz.subject.infrastructure;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "subjects")
@Entity
public class Subject extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;


    public static Subject from(SubjectDomain domain)
    {
        Subject subject = new Subject();
        subject.id = domain.getId();
        subject.name = domain.getName();
        return subject;
    }

    public SubjectDomain toModel()
    {
        return SubjectDomain.builder()
                .id(id)
                .name(name)
                .build();
    }

}
