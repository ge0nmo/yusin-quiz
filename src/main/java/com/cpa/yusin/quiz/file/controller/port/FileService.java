package com.cpa.yusin.quiz.file.controller.port;

import org.springframework.web.multipart.MultipartFile;

public interface FileService
{
    String save(MultipartFile file);

    String generatePresignedUrl(String objectKey);

    String saveByteArray(byte[] fileData, String filename, String contentType);
}
