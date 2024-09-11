package com.cpa.yusin.quiz.file.domain;

import lombok.Builder;
import lombok.Getter;

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


}
