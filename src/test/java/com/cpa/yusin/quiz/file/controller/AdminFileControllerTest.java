package com.cpa.yusin.quiz.file.controller;

import com.cpa.yusin.quiz.file.controller.port.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminFileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private AdminFileController adminFileController;

    @Test
    void shouldExtractDecodedObjectKeyAndReturnPresignedUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "question image.png",
                "image/png",
                "content".getBytes()
        );

        when(fileService.save(file))
                .thenReturn("https://bucket.s3.ap-northeast-2.amazonaws.com/post/question%20image.png");
        when(fileService.generatePresignedUrl("post/question image.png"))
                .thenReturn("https://signed.example.com/post/question-image");

        String response = adminFileController.save(file);

        assertThat(response).isEqualTo("https://signed.example.com/post/question-image");
        verify(fileService).generatePresignedUrl("post/question image.png");
    }

    @Test
    void shouldFallbackToRawUrlWhenObjectKeyExtractionFails() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "broken.png",
                "image/png",
                "content".getBytes()
        );
        String malformedUrl = "https://bad host.com/file.png";

        when(fileService.save(file)).thenReturn(malformedUrl);
        when(fileService.generatePresignedUrl(malformedUrl))
                .thenReturn("https://signed.example.com/fallback");

        String response = adminFileController.save(file);

        assertThat(response).isEqualTo("https://signed.example.com/fallback");
        verify(fileService).generatePresignedUrl(malformedUrl);
    }
}
