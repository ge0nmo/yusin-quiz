package com.cpa.yusin.quiz.global.jwt;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService
{
    private final SecretKey key;

    @Value("${jwt.token.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.token.refresh-token.expiration}")
    private long refreshTokenExpiration;


    public JwtServiceImpl(@Value("${jwt.token.secretKey}") String secretKey)
    {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @Override
    public String createAccessToken(Map<String, Object> claims, String email)
    {
        return createToken(claims, email, accessTokenExpiration);
    }


    private String createToken(Map<String, Object> claims, String email, long expiration)
    {
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    @Override
    public boolean validateToken(String token, MemberDetails memberDetails)
    {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        String email = extractSubject(token);

        return !expirationDate.before(new Date()) && memberDetails.getUsername().equals(email);
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

    private Claims extractAllClaims(String token)
    {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
