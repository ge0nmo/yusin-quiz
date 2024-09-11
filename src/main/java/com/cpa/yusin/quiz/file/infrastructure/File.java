package com.cpa.yusin.quiz.file.infrastructure;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
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
