package com.cpa.yusin.quiz.file.infrastructure;

import com.cpa.yusin.quiz.file.domain.FileDomain;
import com.cpa.yusin.quiz.file.service.port.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class FileRepositoryImpl implements FileRepository
{
    private final FileJpaRepository fileJpaRepository;

    @Override
    public FileDomain save(FileDomain fileDomain)
    {
        return null;
    }
}
