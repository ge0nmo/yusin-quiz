package com.cpa.yusin.quiz.file.mapper;

import com.cpa.yusin.quiz.file.controller.dto.response.FileResponse;
import com.cpa.yusin.quiz.file.domain.FileDomain;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileMapper
{
    public FileDomain toFileDomain(String url, String uniqueFilename, MultipartFile file)
    {
        return FileDomain.builder()
                .url(url)
                .size(file.getSize())
                .contentType(file.getContentType())
                .originalName(file.getOriginalFilename())
                .storedName(uniqueFilename)
                .contentType(file.getContentType())
                .build();
    }

    public FileResponse domainToFileResponse(FileDomain fileDomain)
    {
        return FileResponse.builder()
                .url(fileDomain.getUrl())
                .build();
    }
}
