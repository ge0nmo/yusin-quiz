package com.cpa.yusin.quiz.global.jwt;

import com.cpa.yusin.quiz.member.domain.Member;

public interface JwtService {
    String createAccessToken(Member member);
    String createRefreshToken(Member member);
    String extractSubject(String token);
    boolean isValidAccessToken(String token, Member member);
    boolean isValidRefreshToken(String token, Member member);
}
