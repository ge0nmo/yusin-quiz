package com.cpa.yusin.quiz.file.service.port;

import com.cpa.yusin.quiz.file.domain.File;

public interface FileRepository
{
    File save(File file);
}
