package com.cpa.yusin.quiz.member.integration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GoogleLoginIntegrationTest {
    private static final String EXTERNAL_TOKEN = "external-token-must-not-leak";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Test
    void blankIdTokenReturnsValidationCodeAndRedactsRejectedValue() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.valueErrors[0].descriptor").value("idToken"))
                .andExpect(jsonPath("$.valueErrors[0].rejectedValue").value("[REDACTED]"));
    }

    @Test
    void invalidOrExpiredGoogleTokenReturnsUnauthorizedCode() throws Exception {
        given(googleIdTokenVerifier.verify(EXTERNAL_TOKEN)).willReturn(null);

        performGoogleLogin(EXTERNAL_TOKEN)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("INVALID_SOCIAL_TOKEN"))
                .andExpect(jsonPath("$.message").value("유효하지 않거나 만료된 소셜 로그인 토큰입니다."))
                .andExpect(content().string(not(containsString(EXTERNAL_TOKEN))));
    }

    @Test
    void malformedGoogleTokenDoesNotExposeParserDetails() throws Exception {
        given(googleIdTokenVerifier.verify(EXTERNAL_TOKEN))
                .willThrow(new IOException("parse failure: " + EXTERNAL_TOKEN));

        performGoogleLogin(EXTERNAL_TOKEN)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_SOCIAL_TOKEN"))
                .andExpect(content().string(not(containsString(EXTERNAL_TOKEN))))
                .andExpect(content().string(not(containsString("parse failure"))));
    }

    @Test
    void unverifiedGoogleEmailReturnsBadRequestCode() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload()
                .setEmail("user@example.com")
                .setEmailVerified(false);
        GoogleIdToken googleIdToken = idToken(payload);
        given(googleIdTokenVerifier.verify(EXTERNAL_TOKEN)).willReturn(googleIdToken);

        performGoogleLogin(EXTERNAL_TOKEN)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_SOCIAL_PROFILE"));
    }

    @Test
    void malformedGoogleEmailReturnsBadRequestCode() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload()
                .setEmail("not-an-email")
                .setEmailVerified(true);
        GoogleIdToken googleIdToken = idToken(payload);
        given(googleIdTokenVerifier.verify(EXTERNAL_TOKEN)).willReturn(googleIdToken);

        performGoogleLogin(EXTERNAL_TOKEN)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_SOCIAL_PROFILE"));
    }

    private org.springframework.test.web.servlet.ResultActions performGoogleLogin(String idToken) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "idToken": "%s"
                        }
                        """.formatted(idToken)));
    }

    private GoogleIdToken idToken(GoogleIdToken.Payload payload) {
        GoogleIdToken idToken = mock(GoogleIdToken.class);
        given(idToken.getPayload()).willReturn(payload);
        return idToken;
    }
}
