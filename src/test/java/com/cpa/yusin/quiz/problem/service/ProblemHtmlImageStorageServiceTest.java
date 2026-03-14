package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.mock.FakeUuidHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProblemHtmlImageStorageServiceTest {

    private FileService fileService;
    private ProblemHtmlImageStorageService imageStorageService;

    @BeforeEach
    void setUp() {
        fileService = Mockito.mock(FileService.class);
        imageStorageService = new ProblemHtmlImageStorageService(
                fileService,
                new FakeUuidHolder("test-image-uuid")
        );
    }

    @Test
    @DisplayName("Base64 이미지가 포함된 HTML 은 업로드 URL 로 치환된다")
    void replaceEmbeddedImages_whenBase64ImageExists_thenUploadAndReplace() {
        String html = "<p>문제</p><img src=\"data:image/png;base64,QUJDRA==\">";

        when(fileService.saveByteArray(any(), eq("test-image-uuid.png"), eq("image/png")))
                .thenReturn("https://cdn.example.com/test-image-uuid.png");

        String result = imageStorageService.replaceEmbeddedImages(html);

        assertThat(result).contains("https://cdn.example.com/test-image-uuid.png");
        assertThat(result).contains("max-width: 100%; height: auto;");
        verify(fileService).saveByteArray(any(), eq("test-image-uuid.png"), eq("image/png"));
    }

    @Test
    @DisplayName("손상된 Base64 이미지가 오면 원본 src 를 유지한다")
    void replaceEmbeddedImages_whenBase64ImageIsMalformed_thenLeaveSourceUntouched() {
        String html = "<img src=\"data:image/png;base64,###invalid###\">";

        String result = imageStorageService.replaceEmbeddedImages(html);

        assertThat(result).contains("data:image/png;base64,###invalid###");
    }
}
