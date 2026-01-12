package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;
import com.cpa.yusin.quiz.member.service.port.SocialTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialLoadService
{
    private final List<SocialTokenVerifier> verifiers;

    public SocialProfile getSocialProfile(Platform platform, String token)
    {
        return verifiers.stream()
                .filter(verifier -> verifier.support(platform))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 소셜 플랫폼입니다: " + platform))
                .verify(token);
    }
}