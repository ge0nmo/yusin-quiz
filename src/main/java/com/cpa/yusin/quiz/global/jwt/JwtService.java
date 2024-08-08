package com.cpa.yusin.quiz.global.jwt;

import com.cpa.yusin.quiz.global.details.MemberDetails;

public interface JwtService
{
    boolean validateToken(String token, MemberDetails memberDetails);

    String extractSubject(String token);
}
