package com.cpa.yusin.quiz.file.controller;

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
public class AdminFileController {
    private final FileService fileService;

    @PostMapping
    public String save(@RequestParam("file") MultipartFile file) {
        String rawUrl = fileService.save(file);
        String objectKey = extractObjectKeyFromUrl(rawUrl);

        return fileService.generatePresignedUrl(objectKey);
    }

    // Next.js 관리자 화면은 업로드 직후 미리보기 URL이 즉시 필요함.
    private String extractObjectKeyFromUrl(String fullUrl) {
        try {
            URI uri = URI.create(fullUrl);
            String path = uri.getPath();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            return fullUrl;
        }
    }
}
