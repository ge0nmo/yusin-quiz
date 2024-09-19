package com.cpa.yusin.quiz.file.controller.port;

import com.cpa.yusin.quiz.file.controller.dto.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService
{
    FileResponse save(MultipartFile file);
}
