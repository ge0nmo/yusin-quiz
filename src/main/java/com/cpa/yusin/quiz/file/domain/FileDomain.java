package com.cpa.yusin.quiz.file.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class FileDomain
{
    private Long id;
    private String originalName;
    private String url;
    private String storedName;
    private String contentType;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
