package com.cpa.yusin.quiz.file.service.port;

import com.cpa.yusin.quiz.file.domain.FileDomain;

public interface FileRepository
{
    FileDomain save(FileDomain fileDomain);
}
