package com.cpa.yusin.quiz.global.jwt;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService
{
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ISSUED_AT_MILLIS_CLAIM = "issuedAtMillis";
    private static final String MEMBER_ID_CLAIM = "memberId";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey key;
    private final ClockHolder clockHolder;

    @Value("${jwt.token.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.token.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public JwtServiceImpl(@Value("${jwt.token.secretKey}") String secretKey,
                          ClockHolder clockHolder)
    {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.clockHolder = clockHolder;
    }

    @Override
    public String createAccessToken(String email, long memberId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        claims.put(MEMBER_ID_CLAIM, memberId);
        return createToken(claims, email, accessTokenExpiration);
    }

    @Override
    public String createRefreshToken(String email, long memberId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        claims.put(MEMBER_ID_CLAIM, memberId);
        return createToken(claims, email, refreshTokenExpiration);
    }

    @Override
    public boolean isAccessToken(String token) {
        return hasTokenType(token, ACCESS_TOKEN_TYPE);
    }

    @Override
    public boolean isRefreshToken(String token) {
        return hasTokenType(token, REFRESH_TOKEN_TYPE);
    }


    private String createToken(Map<String, Object> claims, String email, long expiration)
    {
        long issuedAtMillis = clockHolder.getCurrentTime();
        claims.put(ISSUED_AT_MILLIS_CLAIM, issuedAtMillis);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(issuedAtMillis))
                .expiration(new Date(issuedAtMillis + expiration))
                .signWith(key)
                .compact();
    }

    @Override
    public boolean isValidToken(String token, MemberDetails memberDetails)
    {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        String email = extractSubject(token);

        return isAccessToken(token)
                && !expirationDate.before(currentDate())
                && memberDetails.getUsername().equals(email)
                && isTokenIssuedTo(token, memberDetails);
    }

    @Override
    public boolean isTokenIssuedTo(String token, MemberDetails memberDetails) {
        if (memberDetails == null || memberDetails.getMember() == null) {
            return false;
        }
        Number tokenMemberId = extractClaim(token, claims -> claims.get(MEMBER_ID_CLAIM, Number.class));
        if (tokenMemberId != null) {
            return memberDetails.getMember().getId() != null
                    && tokenMemberId.longValue() == memberDetails.getMember().getId();
        }

        if (memberDetails.getMember().getCreatedAt() == null) {
            return false;
        }

        Number exactIssuedAt = extractClaim(token, claims -> claims.get(ISSUED_AT_MILLIS_CLAIM, Number.class));
        long memberCreatedAtMillis = memberDetails.getMember().getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        if (exactIssuedAt != null) {
            return exactIssuedAt.longValue() >= memberCreatedAtMillis;
        }

        Date legacyIssuedAt = extractClaim(token, Claims::getIssuedAt);
        if (legacyIssuedAt == null) {
            return false;
        }

        // Standard JWT iat has second precision. Equality with the member-creation
        // second is ambiguous after same-email re-registration, so deployed legacy
        // tokens from that one-second boundary are conservatively rejected.
        long memberCreatedAtSecond = (memberCreatedAtMillis / 1_000L) * 1_000L;
        return legacyIssuedAt.getTime() > memberCreatedAtSecond;
    }

    @Override
    public boolean isTokenExpired(String token)
    {
        try {
            return extractClaim(token, Claims::getExpiration).before(currentDate());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }


    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String extractSubject(String token)
    {
        return extractClaim(token, Claims::getSubject);
    }

    private Date currentDate() {
        return new Date(clockHolder.getCurrentTime());
    }

    private Claims extractAllClaims(String token)
    {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean hasTokenType(String token, String expectedType) {
        Object tokenType = extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM));
        return expectedType.equals(tokenType);
    }
}
