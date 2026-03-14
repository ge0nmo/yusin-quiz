package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.global.security.CustomAuthenticationProvider;
import com.cpa.yusin.quiz.member.controller.dto.request.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.TokenResponse;
import com.cpa.yusin.quiz.member.controller.mapper.MemberMapper;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.port.MemberValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final CustomAuthenticationProvider authenticationProvider;
    private final MemberMapper memberMapper;
    private final MemberValidator memberValidator;
    private final RandomNicknameGenerator randomNicknameGenerator;
    private final UuidHolder uuidHolder;

    @Autowired
    public AuthenticationServiceImpl(PasswordEncoder passwordEncoder,
                                     JwtService jwtService,
                                     MemberRepository memberRepository,
                                     CustomAuthenticationProvider authenticationProvider,
                                     MemberMapper memberMapper,
                                     MemberValidator memberValidator,
                                     RandomNicknameGenerator randomNicknameGenerator,
                                     @Qualifier("systemUuidHolder") UuidHolder uuidHolder) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.memberRepository = memberRepository;
        this.authenticationProvider = authenticationProvider;
        this.memberMapper = memberMapper;
        this.memberValidator = memberValidator;
        this.randomNicknameGenerator = randomNicknameGenerator;
        this.uuidHolder = uuidHolder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return processLogin(request.getEmail(), request.getPassword());
    }

    @Override
    public LoginResponse loginAsAdmin(String email, String password) {
        LoginResponse response = processLogin(email, password);

        if (response.getRole() != Role.ADMIN) {
            log.warn("일반 유저가 관리자 페이지 접속 시도: {}", email);
            throw new MemberException(ExceptionMessage.NO_AUTHORIZATION);
        }

        return response;
    }

    private LoginResponse processLogin(String email, String password) {
        Authentication auth = authenticationProvider
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));
        MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();

        Member member = memberDetails.getMember();

        String accessToken = jwtService.createAccessToken(member.getEmail());
        String refreshToken = jwtService.createRefreshToken(member.getEmail());

        return LoginResponse.from(member.getId(), member.getEmail(), member.getUsername(), member.getRole(),
                accessToken, refreshToken);
    }

    @Override
    public MemberCreateResponse signUp(MemberCreateRequest request) {
        memberValidator.validateEmail(request.getEmail());

        Member member = Member.fromHome(request.getEmail(), passwordEncoder.encode(request.getPassword()),
                request.getUsername());
        member = memberRepository.save(member);

        return memberMapper.toMemberCreateResponse(member);
    }

    @Override
    @Transactional
    public LoginResponse socialLogin(SocialProfile socialProfile) {
        Member member = memberRepository.findByEmail(socialProfile.getEmail())
                .orElseGet(() -> registerSocialMember(socialProfile));

        String accessToken = jwtService.createAccessToken(member.getEmail());
        String refreshToken = jwtService.createRefreshToken(member.getEmail());

        return LoginResponse.from(member.getId(), member.getEmail(), member.getUsername(), member.getRole(),
                accessToken, refreshToken);
    }

    private Member registerSocialMember(SocialProfile profile) {
        String nickname = generateUniqueNickname();

        Member newMember = Member.builder()
                .email(profile.getEmail())
                .username(nickname)
                .password(passwordEncoder.encode(uuidHolder.getRandom()))
                .role(Role.USER)
                .platform(profile.getPlatform())
                .build();
        return memberRepository.save(newMember);
    }

    private String generateUniqueNickname() {
        String nickname = randomNicknameGenerator.generate();
        int maxRetries = 20;

        for (int i = 0; i < maxRetries; i++) {
            if (!memberRepository.existsByUsername(nickname)) {
                return nickname;
            }
            nickname = randomNicknameGenerator.generate();
        }

        return nickname + uuidHolder.getRandom().substring(0, 5);
    }

    @Override
    public TokenResponse refreshAccessToken(String refreshToken) {
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new MemberException(ExceptionMessage.REFRESH_TOKEN_EXPIRED);
        }

        String email = jwtService.extractSubject(refreshToken);
        String accessToken = jwtService.createAccessToken(email);
        String newRefreshToken = jwtService.createRefreshToken(email);

        return new TokenResponse(accessToken, newRefreshToken);
    }
}
