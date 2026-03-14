package com.cpa.yusin.quiz.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Profile("test") // "test" 프로파일에서만 활성화
@Configuration
public class S3MockConfig
{
    // 1. S3Client (파일 업로드/삭제용) Mock
    @Bean
    public S3Client s3Client() {
        S3Client mockClient = Mockito.mock(S3Client.class);
        S3Utilities mockUtilities = Mockito.mock(S3Utilities.class);

        // s3Client.utilities() 호출 시 mockUtilities 반환
        Mockito.when(mockClient.utilities()).thenReturn(mockUtilities);

        try {
            URL mockUrl = URI.create("https://mock-s3-url.com/file.png").toURL();

            Mockito.when(mockUtilities.getUrl(Mockito.any(GetUrlRequest.class)))
                    .thenReturn(mockUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Mock URL 생성 실패", e);
        }

        return mockClient;
    }

    // 2. S3Presigner (URL 발급용) Mock
    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner mockPresigner = Mockito.mock(S3Presigner.class);

        // presignGetObject 호출 시 반환될 가짜 응답 객체
        PresignedGetObjectRequest mockPresignedRequest = Mockito.mock(PresignedGetObjectRequest.class);

        try {
            URL mockUrl = URI.create("https://mock-s3-url.com/presigned-file.png?signature=fake").toURL();

            Mockito.when(mockPresignedRequest.url()).thenReturn(mockUrl);

            Mockito.when(mockPresigner.presignGetObject(Mockito.any(GetObjectPresignRequest.class)))
                    .thenReturn(mockPresignedRequest);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Mock URL 생성 실패", e);
        }

        return mockPresigner;
    }
}
