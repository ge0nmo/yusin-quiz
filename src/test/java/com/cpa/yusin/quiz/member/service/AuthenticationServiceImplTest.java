package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock
    private UuidHolder uuidHolder;

    @Test
    @DisplayName("관리자 로그인은 ADMIN 회원일 때만 성공해야 함")
    void loginAsAdmin_AdminMember_Success() {
        Member admin = Member.builder()
                .id(10L)
                .email("admin@test.com")
                .username("admin")
                .password("encoded-password")
                .role(Role.ADMIN)
                .platform(Platform.HOME)
                .build();
        MemberDetails memberDetails = new MemberDetails(admin, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());

        given(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtService.createAccessToken(admin.getEmail())).willReturn("access-token");
        given(jwtService.createRefreshToken(admin.getEmail())).willReturn("refresh-token");

        LoginResponse response = authenticationService.loginAsAdmin(admin.getEmail(), "password");

        assertThat(response.getId()).isEqualTo(admin.getId());
        assertThat(response.getEmail()).isEqualTo(admin.getEmail());
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(authenticationProvider).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("일반 회원은 관리자 로그인에 실패해야 함")
    void loginAsAdmin_UserMember_ThrowsNoAuthorization() {
        Member user = Member.builder()
                .id(11L)
                .email("user@test.com")
                .username("user")
                .password("encoded-password")
                .role(Role.USER)
                .platform(Platform.HOME)
                .build();
        MemberDetails memberDetails = new MemberDetails(user, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());

        given(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtService.createAccessToken(user.getEmail())).willReturn("access-token");
        given(jwtService.createRefreshToken(user.getEmail())).willReturn("refresh-token");

        assertThatThrownBy(() -> authenticationService.loginAsAdmin(user.getEmail(), "password"))
                .isInstanceOf(MemberException.class)
                .satisfies(exception -> assertThat(((MemberException) exception).getExceptionMessage())
                        .isEqualTo(ExceptionMessage.NO_AUTHORIZATION));
    }

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
        given(uuidHolder.getRandom()).willReturn("generated-uuid-12345");
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

        given(uuidHolder.getRandom()).willReturn("generated-uuid-67890");
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

    @Test
    @DisplayName("랜덤 닉네임이 계속 충돌하면 UUID 접미사로 fallback 한다")
    void socialLogin_NewMember_FallbackWithUuidSuffix() {
        String email = "fallback@gmail.com";
        SocialProfile profile = SocialProfile.builder()
                .email(email)
                .name("Fallback User")
                .platform(Platform.GOOGLE)
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());
        given(randomNicknameGenerator.generate()).willReturn("중복닉네임");
        given(memberRepository.existsByUsername("중복닉네임")).willReturn(true);
        given(uuidHolder.getRandom())
                .willReturn("abcde-uuid-fallback")
                .willReturn("password-seed-uuid");
        given(passwordEncoder.encode(any())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            return Member.builder()
                    .id(3L)
                    .email(member.getEmail())
                    .username(member.getUsername())
                    .password(member.getPassword())
                    .role(member.getRole())
                    .platform(member.getPlatform())
                    .build();
        });
        given(jwtService.createAccessToken(email)).willReturn("accessToken");
        given(jwtService.createRefreshToken(email)).willReturn("refreshToken");

        LoginResponse response = authenticationService.socialLogin(profile);

        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getUsername()).isEqualTo("중복닉네임abcde");
        verify(randomNicknameGenerator, org.mockito.Mockito.times(21)).generate();
        verify(uuidHolder, org.mockito.Mockito.times(2)).getRandom();
    }

    @Test
    @DisplayName("리프레시 토큰이 만료되면 재발급을 거부한다")
    void refreshAccessToken_whenTokenExpired_thenThrow() {
        given(jwtService.isRefreshToken("expired-token")).willReturn(true);
        given(jwtService.isTokenExpired("expired-token")).willReturn(true);

        assertThatThrownBy(() -> authenticationService.refreshAccessToken("expired-token"))
                .isInstanceOf(MemberException.class)
                .satisfies(exception -> assertThat(((MemberException) exception).getExceptionMessage())
                        .isEqualTo(ExceptionMessage.REFRESH_TOKEN_EXPIRED));
    }

    @Test
    @DisplayName("액세스 토큰으로는 리프레시 재발급을 할 수 없다")
    void refreshAccessToken_whenTokenIsNotRefreshToken_thenThrow() {
        given(jwtService.isRefreshToken("access-token")).willReturn(false);

        assertThatThrownBy(() -> authenticationService.refreshAccessToken("access-token"))
                .isInstanceOf(MemberException.class)
                .satisfies(exception -> assertThat(((MemberException) exception).getExceptionMessage())
                        .isEqualTo(ExceptionMessage.INVALID_REFRESH_TOKEN));
    }

    @Test
    @DisplayName("리프레시 토큰의 사용자가 존재하지 않으면 재발급을 거부한다")
    void refreshAccessToken_whenMemberDoesNotExist_thenThrow() {
        given(jwtService.isRefreshToken("refresh-token")).willReturn(true);
        given(jwtService.isTokenExpired("refresh-token")).willReturn(false);
        given(jwtService.extractSubject("refresh-token")).willReturn("deleted@test.com");
        given(memberRepository.findByEmail("deleted@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.refreshAccessToken("refresh-token"))
                .isInstanceOf(MemberException.class)
                .satisfies(exception -> assertThat(((MemberException) exception).getExceptionMessage())
                        .isEqualTo(ExceptionMessage.USER_NOT_FOUND));
    }
}
