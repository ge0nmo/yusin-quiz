package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.file.controller.port.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@RequestMapping("/api/admin/file")
@RestController
public class FileController
{
    private final FileService fileService;

    @PostMapping
    public String save(@RequestParam(value = "file") MultipartFile file)
    {
        // 1. S3에 원본 업로드 (Raw URL 반환됨)
        String rawUrl = fileService.save(file);

        // 2. 방금 올린 파일의 Key 추출
        String objectKey = extractObjectKeyFromUrl(rawUrl);

        // 3. [핵심] 브라우저가 즉시 볼 수 있게 Presigned URL 생성하여 반환
        return fileService.generatePresignedUrl(objectKey);
    }

    // URL에서 Key만 발라내는 헬퍼 메서드
    private String extractObjectKeyFromUrl(String fullUrl) {
        try {
            URI uri = URI.create(fullUrl);
            String path = uri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 파싱 실패시 그냥 원본 리턴 (어차피 403 나겠지만 로직 보호용)
            return fullUrl;
        }
    }
}
