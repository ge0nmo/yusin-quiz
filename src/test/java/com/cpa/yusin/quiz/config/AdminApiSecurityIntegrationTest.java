package com.cpa.yusin.quiz.config;

import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private SubjectService subjectService;

    @Test
    @DisplayName("관리자 로그인 API는 인증 없이 접근 가능해야 함")
    void adminLoginShouldBeAccessibleWithoutAuthentication() throws Exception {
        given(authenticationService.loginAsAdmin(eq("admin@test.com"), eq("password")))
                .willReturn(LoginResponse.from(
                        1L,
                        "admin@test.com",
                        "admin",
                        Role.ADMIN,
                        "access-token",
                        "refresh-token"
                ));

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@test.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@test.com"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("관리자 API는 비인증 요청을 차단해야 함")
    void adminApiShouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/admin/subject"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROLE_USER는 관리자 API를 사용할 수 없어야 함")
    void adminApiShouldRejectNonAdminUsers() throws Exception {
        mockMvc.perform(get("/api/admin/subject")
                        .with(user("user@test.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROLE_ADMIN은 관리자 API를 사용할 수 있어야 함")
    void adminApiShouldAllowAdminUsers() throws Exception {
        given(subjectService.getAll())
                .willReturn(List.of(SubjectDTO.builder().id(1L).name("회계학").build()));

        mockMvc.perform(get("/api/admin/subject")
                        .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("회계학"));
    }
}
