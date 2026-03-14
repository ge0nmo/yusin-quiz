package com.cpa.yusin.quiz.global.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExceptionAdviceTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionThrowingController())
                .setControllerAdvice(new ExceptionAdvice())
                .build();
    }

    @Test
    void asyncRequestNotUsableExceptionShouldNotSerializeBadRequestBody() throws Exception {
        mockMvc.perform(get("/test/async-abort"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void ioExceptionShouldStillReturnBadRequestErrorResponse() throws Exception {
        mockMvc.perform(get("/test/io-error"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("disk failure"));
    }

    @Test
    void brokenPipeIOExceptionShouldBeDowngradedToEmptyResponse() throws Exception {
        mockMvc.perform(get("/test/broken-pipe"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @RestController
    static class ExceptionThrowingController {

        @GetMapping("/test/async-abort")
        public String asyncAbort() throws IOException {
            throw new AsyncRequestNotUsableException("ServletOutputStream failed to write: Broken pipe");
        }

        @GetMapping("/test/io-error")
        public String ioError() throws IOException {
            throw new IOException("disk failure");
        }

        @GetMapping("/test/broken-pipe")
        public String brokenPipe() throws IOException {
            throw new IOException("Broken pipe");
        }
    }
}
