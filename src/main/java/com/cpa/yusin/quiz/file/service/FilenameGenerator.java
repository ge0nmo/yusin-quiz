package com.cpa.yusin.quiz.file.service;

import com.cpa.yusin.quiz.common.service.UuidHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FilenameGenerator
{
    private final UuidHolder uuidHolder;

    public FilenameGenerator(@Qualifier("filenameUuidHolder") UuidHolder uuidHolder)
    {
        this.uuidHolder = uuidHolder;
    }


    public String createStoreFileName(String originalFileName)
    {
        String uniqueFilename = uuidHolder.getRandom();
        String fileType = extractType(originalFileName);

        return uniqueFilename + "." + fileType;
    }

    private String extractType(String filename)
    {
        int location = filename.lastIndexOf('.');

        return filename.substring(location + 1);
    }
}
