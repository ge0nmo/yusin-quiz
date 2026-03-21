package com.cpa.yusin.quiz.config;

import com.cpa.yusin.quiz.member.controller.dto.response.TokenResponse;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("보호된 북마크 상태 API는 비인증 요청에 401을 반환해야 한다")
    void bookmarkStatusShouldReturnUnauthorizedForUnauthenticatedRequests() throws Exception {
        mockMvc.perform(post("/api/v1/bookmarks/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemIds": [1, 2]
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/bookmarks/status"));
    }

    @Test
    @DisplayName("보호된 질문 작성 API는 비인증 요청에 401을 반환해야 한다")
    void questionCreateShouldReturnUnauthorizedForUnauthenticatedRequests() throws Exception {
        mockMvc.perform(post("/api/v1/problem/1/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "질문 제목",
                                  "content": "질문 내용"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"))
                .andExpect(jsonPath("$.path").value("/api/v1/problem/1/question"));
    }

    @Test
    @DisplayName("보호된 학습 시작 API는 비인증 요청에 401을 반환해야 한다")
    void studyStartShouldReturnUnauthorizedForUnauthenticatedRequests() throws Exception {
        mockMvc.perform(post("/api/v1/study/exam/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "examId": 1,
                                  "mode": "PRACTICE"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"))
                .andExpect(jsonPath("$.path").value("/api/v1/study/exam/start"));
    }

    @Test
    @DisplayName("공개 GET 엔드포인트는 인증 없이 접근 가능해야 한다")
    void publicGetEndpointShouldRemainAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/hc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.env").exists());
    }

    @Test
    @DisplayName("인증 관련 공개 API는 인증 없이 접근 가능해야 한다")
    void authEndpointShouldRemainAccessibleWithoutAuthentication() throws Exception {
        given(authenticationService.refreshAccessToken(eq("refresh-token")))
                .willReturn(new TokenResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }
}
