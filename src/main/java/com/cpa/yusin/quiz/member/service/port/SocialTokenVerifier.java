package com.cpa.yusin.quiz.member.service.port;

import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;

public interface SocialTokenVerifier {
    // 어떤 플랫폼을 지원하는지 확인 (구글? 애플?)
    boolean support(Platform platform);
    
    // 토큰을 검증하고 유저 정보를 반환
    SocialProfile verify(String token);
}