package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.global.security.CustomAuthenticationProvider;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.mapper.MemberMapper;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.port.MemberValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CustomAuthenticationProvider authenticationProvider;
    @Mock
    private MemberMapper memberMapper;
    @Mock
    private MemberValidator memberValidator;
    @Mock
    private RandomNicknameGenerator randomNicknameGenerator;

    @Test
    @DisplayName("소셜 로그인 시 신규 회원이면 랜덤 닉네임으로 저장된다")
    void socialLogin_NewMember_RandomNickname() {
        // given
        String email = "test@gmail.com";
        String googleName = "Google Name";
        String randomNickname = "행복한사자1234";
        SocialProfile profile = SocialProfile.builder()
                .email(email)
                .name(googleName)
                .platform(Platform.GOOGLE)
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());
        given(randomNicknameGenerator.generate()).willReturn(randomNickname);
        given(memberRepository.existsByUsername(randomNickname)).willReturn(false); // 닉네임 중복 없음
        given(passwordEncoder.encode(any())).willReturn("encodedPassword");

        // Mock save to return the member with the random nickname and ID
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            return Member.builder()
                    .id(1L)
                    .email(member.getEmail())
                    .username(member.getUsername())
                    .password(member.getPassword())
                    .role(member.getRole())
                    .platform(member.getPlatform())
                    .build();
        });

        given(jwtService.createAccessToken(email)).willReturn("accessToken");
        given(jwtService.createRefreshToken(email)).willReturn("refreshToken");

        // when
        authenticationService.socialLogin(profile);

        // then
        verify(randomNicknameGenerator).generate();
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("소셜 로그인 시 닉네임 중복 발생하면 재시도한다")
    void socialLogin_NewMember_RetryOnCollision() {
        // given
        String email = "test2@gmail.com";
        String googleName = "Google Name 2";
        String collisionNickname = "행복한사자"; // 첫 번째 생성 (중복)
        String uniqueNickname = "즐거운호랑이"; // 두 번째 생성 (성공)

        SocialProfile profile = SocialProfile.builder()
                .email(email)
                .name(googleName)
                .platform(Platform.GOOGLE)
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

        // 첫 번째 호출 -> collisionNickname, 두 번째 호출 -> uniqueNickname
        given(randomNicknameGenerator.generate())
                .willReturn(collisionNickname)
                .willReturn(uniqueNickname);

        // 첫 번째 검사 -> 중복(true), 두 번째 검사 -> 중복아님(false)
        given(memberRepository.existsByUsername(collisionNickname)).willReturn(true);
        given(memberRepository.existsByUsername(uniqueNickname)).willReturn(false);

        given(passwordEncoder.encode(any())).willReturn("encodedPassword");

        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            return Member.builder()
                    .id(2L)
                    .email(member.getEmail())
                    .username(member.getUsername())
                    .password(member.getPassword())
                    .role(member.getRole())
                    .platform(member.getPlatform())
                    .build();
        });

        given(jwtService.createAccessToken(email)).willReturn("accessToken");
        given(jwtService.createRefreshToken(email)).willReturn("refreshToken");

        // when
        authenticationService.socialLogin(profile);

        // then
        // generate()가 2번 호출되었는지 확인
        verify(randomNicknameGenerator, org.mockito.Mockito.times(2)).generate();
    }

    @Test
    @DisplayName("소셜 로그인 시 이미 가입된 회원이면 닉네임이 변경되지 않는다")
    void socialLogin_ExistingMember_NoChange() {
        // given
        String email = "existing@gmail.com";
        String existingNickname = "기존닉네임";

        SocialProfile profile = SocialProfile.builder()
                .email(email)
                .name("New Google Name") // 구글 이름이 바뀌어도
                .platform(Platform.GOOGLE)
                .build();

        Member existingMember = Member.builder()
                .id(1L)
                .email(email)
                .username(existingNickname) // 기존 닉네임 유지 확인
                .password("password")
                .role(Role.USER)
                .platform(Platform.GOOGLE)
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(existingMember));

        given(jwtService.createAccessToken(email)).willReturn("accessToken");
        given(jwtService.createRefreshToken(email)).willReturn("refreshToken");

        // when
        LoginResponse response = authenticationService.socialLogin(profile);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        // 저장 로직이 호출되지 않아야 함 (닉네임 생성도 안 함)
        verify(memberRepository, org.mockito.Mockito.never()).save(any(Member.class));
        verify(randomNicknameGenerator, org.mockito.Mockito.never()).generate();
    }
}
