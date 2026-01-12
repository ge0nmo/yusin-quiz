package com.cpa.yusin.quiz.member.infrastructure;

import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;
import com.cpa.yusin.quiz.member.service.port.SocialTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class GoogleTokenVerifier implements SocialTokenVerifier
{

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Override
    public boolean support(Platform platform) {
        return Platform.GOOGLE == platform;
    }

    @Override
    public SocialProfile verify(String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken == null) {
                throw new IllegalArgumentException("유효하지 않은 Google ID Token 입니다.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            
            return SocialProfile.builder()
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .platform(Platform.GOOGLE)
                    .build();

        } catch (Exception e) {
            log.error("Google Token Verification Failed", e);
            throw new IllegalArgumentException("구글 토큰 검증 실패: " + e.getMessage());
        }
    }
}