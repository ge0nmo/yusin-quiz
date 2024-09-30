package com.cpa.yusin.quiz.file.infrastructure;

import com.cpa.yusin.quiz.file.domain.File;
import com.cpa.yusin.quiz.file.service.port.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class FileRepositoryImpl implements FileRepository
{
    private final FileJpaRepository fileJpaRepository;

    @Override
    public File save(File file)
    {
        return fileJpaRepository.save(file);
    }
}
