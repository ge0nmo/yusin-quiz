package com.cpa.yusin.quiz.file.infrastructure;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.file.domain.FileDomain;
import jakarta.persistence.*;
import lombok.Builder;
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

    public static File from(FileDomain fileDomain)
    {
        File file = new File();
        file.id = fileDomain.getId();
        file.originalName = fileDomain.getOriginalName();
        file.url = fileDomain.getUrl();
        file.storedName = fileDomain.getStoredName();
        file.contentType = fileDomain.getContentType();
        file.size = fileDomain.getSize();
        file.setCreatedAt(fileDomain.getCreatedAt());
        file.setUpdatedAt(fileDomain.getUpdatedAt());
        return file;
    }

    public FileDomain toModel()
    {
        return FileDomain.builder()
                .id(this.id)
                .originalName(this.originalName)
                .url(this.url)
                .storedName(this.storedName)
                .contentType(this.contentType)
                .size(this.size)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }
}
