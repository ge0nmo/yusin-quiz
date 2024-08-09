package com.cpa.yusin.quiz.global.jwt;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import io.jsonwebtoken.Claims;

import java.util.Map;
import java.util.function.Function;

public interface JwtService
{
    String createAccessToken(Map<String, Object> claims, String email);

    boolean validateToken(String token, MemberDetails memberDetails);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String extractSubject(String token);
}
