package com.cpa.yusin.quiz.member.infrastructure;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;
import com.cpa.yusin.quiz.member.service.port.SocialTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier implements SocialTokenVerifier {
    private final GoogleIdTokenVerifier verifier;

    @Override
    public boolean support(Platform platform) {
        return Platform.GOOGLE == platform;
    }

    @Override
    public SocialProfile verify(String token) {
        if (!StringUtils.hasText(token)) {
            throw new MemberException(ExceptionMessage.INVALID_SOCIAL_TOKEN);
        }

        try {
            GoogleIdToken idToken = verifier.verify(token);

            if (idToken == null) {
                throw new MemberException(ExceptionMessage.INVALID_SOCIAL_TOKEN);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            if (payload == null
                    || !StringUtils.hasText(payload.getEmail())
                    || !Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new MemberException(ExceptionMessage.INVALID_SOCIAL_PROFILE);
            }

            return SocialProfile.builder()
                    .email(payload.getEmail())
                    .name(payload.get("name") instanceof String name ? name : null)
                    .platform(Platform.GOOGLE)
                    .build();
        } catch (MemberException e) {
            throw e;
        } catch (GeneralSecurityException | IOException | RuntimeException e) {
            // Verification errors may contain token parsing details. Log only the
            // exception type and return a stable authentication error to the client.
            log.warn("Google ID token verification rejected: reasonType={}", e.getClass().getSimpleName());
            throw new MemberException(ExceptionMessage.INVALID_SOCIAL_TOKEN);
        }
    }
}
