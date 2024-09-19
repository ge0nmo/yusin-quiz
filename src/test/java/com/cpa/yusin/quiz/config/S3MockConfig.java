package com.cpa.yusin.quiz.config;

import com.amazonaws.services.s3.AmazonS3;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3MockConfig extends S3Config
{
    @Bean
    @Override
    public AmazonS3 amazonS3Client()
    {
        return Mockito.mock(AmazonS3.class);
    }
}
