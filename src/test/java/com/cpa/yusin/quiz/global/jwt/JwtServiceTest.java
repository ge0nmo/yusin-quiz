package com.cpa.yusin.quiz.global.jwt;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.config.TestContainer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest
{
    private static final String TEST_SECRET_KEY =
            "thisIsATestSecretKeyUsedOnlyForTesasdfeefsdfewdfesredfesfwqewdasdqrewtingPurposes";
    TestContainer testContainer;
    static String ACCESSTOKEN;
    static String REFRESHTOKEN;
    MemberDetails memberDetails;

    @BeforeEach
    void setUp()
    {
        testContainer = new TestContainer();
        String email = "test@gmail.com";

        testContainer.memberRepository
                .save(Member.builder()
                        .id(1L)
                        .email(email)
                        .password("aaaa")
                        .role(Role.USER)
                        .platform(Platform.HOME)
                        .build());

        ACCESSTOKEN = testContainer.jwtService
                .createAccessToken(email, 1L);
        REFRESHTOKEN = testContainer.jwtService
                .createRefreshToken(email, 1L);

        memberDetails = testContainer.memberDetailsService.loadUserByUsername(email);
    }

    @Test
    void createAccessToken()
    {
        // given
        String email = "test@gmail.com";

        // when
        String result = testContainer.jwtService.createAccessToken(email, 1L);

        // then
        assertNotNull(result);
    }

    @Test
    void tokenType_shouldDistinguishAccessAndRefreshToken() {
        assertThat(testContainer.jwtService.isAccessToken(ACCESSTOKEN)).isTrue();
        assertThat(testContainer.jwtService.isRefreshToken(ACCESSTOKEN)).isFalse();
        assertThat(testContainer.jwtService.isRefreshToken(REFRESHTOKEN)).isTrue();
        assertThat(testContainer.jwtService.isAccessToken(REFRESHTOKEN)).isFalse();
    }

    @Test
    void isValidToken_shouldRejectRefreshTokenForAuthentication() {
        assertThat(testContainer.jwtService.isValidToken(ACCESSTOKEN, memberDetails)).isTrue();
        assertThat(testContainer.jwtService.isValidToken(REFRESHTOKEN, memberDetails)).isFalse();
    }

    @Test
    void accountBoundTokenShouldRejectRecreatedMemberWithSameEmail() {
        Member recreated = Member.builder()
                .id(2L)
                .email("test@gmail.com")
                .password("encoded-password")
                .username("fresh-member")
                .role(Role.USER)
                .platform(Platform.HOME)
                .build();
        MemberDetails recreatedDetails = new MemberDetails(recreated, java.util.Map.of());

        assertThat(testContainer.jwtService.isValidToken(ACCESSTOKEN, recreatedDetails)).isFalse();
        assertThat(testContainer.jwtService.isTokenIssuedTo(REFRESHTOKEN, recreatedDetails)).isFalse();
    }

    @Test
    void legacyTokenFromMemberCreationSecondShouldBeRejectedAsAmbiguous() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 9, 12, 0, 0, 500_000_000);
        MemberDetails details = memberDetailsWithCreatedAt(createdAt);
        long createdSecond = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1_000L * 1_000L;

        String token = legacyAccessToken("test@gmail.com", createdSecond);

        assertThat(testContainer.jwtService.isTokenIssuedTo(token, details)).isFalse();
    }

    @Test
    void legacyTokenFromNextSecondShouldRemainCompatible() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 9, 12, 0, 0, 500_000_000);
        MemberDetails details = memberDetailsWithCreatedAt(createdAt);
        long createdSecond = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1_000L * 1_000L;

        String token = legacyAccessToken("test@gmail.com", createdSecond + 1_000L);

        assertThat(testContainer.jwtService.isTokenIssuedTo(token, details)).isTrue();
    }

    private MemberDetails memberDetailsWithCreatedAt(LocalDateTime createdAt) {
        Member member = Member.builder()
                .id(1L)
                .email("test@gmail.com")
                .password("encoded-password")
                .username("member")
                .role(Role.USER)
                .platform(Platform.HOME)
                .build();
        ReflectionTestUtils.setField(member, "createdAt", createdAt);
        return new MemberDetails(member, java.util.Map.of());
    }

    private String legacyAccessToken(String email, long issuedAtMillis) {
        return Jwts.builder()
                .claim("tokenType", "access")
                .subject(email)
                .issuedAt(new Date(issuedAtMillis))
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes()))
                .compact();
    }

}
