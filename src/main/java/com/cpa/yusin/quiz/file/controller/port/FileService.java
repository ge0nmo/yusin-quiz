package com.cpa.yusin.quiz.file.controller.port;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface FileService
{
    Duration PRESIGNED_URL_TTL = Duration.ofMinutes(180);

    String save(MultipartFile file);

    String generatePresignedUrl(String objectKey);

    String saveByteArray(byte[] fileData, String filename, String contentType);
}
