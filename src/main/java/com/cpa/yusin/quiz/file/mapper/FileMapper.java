package com.cpa.yusin.quiz.file.mapper;

import com.cpa.yusin.quiz.file.controller.dto.response.FileResponse;
import com.cpa.yusin.quiz.file.domain.File;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
public class FileMapper
{
    public File toFileDomain(String url, String uniqueFilename, MultipartFile file)
    {
        if(!StringUtils.hasLength(url) || !StringUtils.hasLength(uniqueFilename) || Objects.isNull(file))
            return null;

        return File.builder()
                .url(url)
                .size(file.getSize())
                .contentType(file.getContentType())
                .originalName(file.getOriginalFilename())
                .storedName(uniqueFilename)
                .contentType(file.getContentType())
                .build();
    }

    public FileResponse domainToFileResponse(File file)
    {
        if(Objects.isNull(file))
            return null;

        return FileResponse.builder()
                .url(file.getUrl())
                .build();
    }
}
