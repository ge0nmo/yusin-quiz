package com.cpa.yusin.quiz.global.jwt;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.member.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {
    private static final String MEMBER_ID = "memberId";
    private static final String TOKEN_TYPE = "tokenType";
    private final SecretKey key;
    private final ClockHolder clockHolder;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtServiceImpl(@Value("${jwt.token.secretKey}") String secretKey,
                          @Value("${jwt.token.access-token-expiration}") long accessExpiration,
                          @Value("${jwt.token.refresh-token-expiration}") long refreshExpiration,
                          ClockHolder clockHolder) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.clockHolder = clockHolder;
    }

    @Override public String createAccessToken(Member member) { return create(member, "access", accessExpiration); }
    @Override public String createRefreshToken(Member member) { return create(member, "refresh", refreshExpiration); }

    @Override
    public String extractSubject(String token) {
        return claims(token).getSubject();
    }

    @Override
    public boolean isValidAccessToken(String token, Member member) {
        return isValid(token, member, "access");
    }

    @Override
    public boolean isValidRefreshToken(String token, Member member) {
        return isValid(token, member, "refresh");
    }

    private boolean isValid(String token, Member member, String expectedType) {
        Claims claims = claims(token);
        Number memberId = claims.get(MEMBER_ID, Number.class);
        return expectedType.equals(claims.get(TOKEN_TYPE, String.class))
                && memberId != null && memberId.longValue() == member.getId()
                && member.getLoginId().equals(claims.getSubject())
                && claims.getExpiration().after(new Date(clockHolder.getCurrentTime()));
    }

    private String create(Member member, String type, long expiration) {
        long now = clockHolder.getCurrentTime();
        return Jwts.builder()
                .claims(Map.of(MEMBER_ID, member.getId(), TOKEN_TYPE, type))
                .subject(member.getLoginId())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(key)
                .compact();
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
