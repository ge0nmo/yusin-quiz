package com.cpa.yusin.quiz.file.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Getter
public class File extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

}
