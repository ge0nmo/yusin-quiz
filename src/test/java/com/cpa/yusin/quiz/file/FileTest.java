package com.cpa.yusin.quiz.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.cpa.yusin.quiz.config.TeardownExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(TeardownExtension.class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class FileTest
{
    @Autowired
    private MockMvc mvc;

    @Autowired
    private AmazonS3 amazonS3;

    @Test
    void uploadFile() throws Exception
    {
        // given
        MockMultipartFile file
                = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());

        when(amazonS3.putObject(any())).thenReturn(new PutObjectResult());
        when(amazonS3.getUrl(any(), any())).thenReturn(URI.create("https://amazonaws.com/test.png").toURL());

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/api/v1/file")
                .file(file));

        // then
        resultActions
                .andExpect(status().isCreated());
    }

}
