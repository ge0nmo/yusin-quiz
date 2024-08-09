package com.cpa.yusin.quiz.security.oauth2;

import com.cpa.yusin.quiz.domain.entity.Member;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.repository.MemberRepository;
import com.cpa.yusin.quiz.security.oauth2.user.OAuth2UserInfo;
import com.cpa.yusin.quiz.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2Service extends DefaultOAuth2UserService
{
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException
    {
        log.info("loadUser");
        OAuth2User oAuth2User = super.loadUser(userRequest);

        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest request, OAuth2User oAuth2User)
    {
        OAuth2UserInfo oAuthUserInfo = OAuth2UserInfoFactory.getOAuthUserInfo(request.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        log.info("attributes = {}", oAuth2User.getAttributes());

        if(!StringUtils.hasLength(oAuthUserInfo.getEmail()))
            throw new RuntimeException("Email not found");

        log.info("email = {}", oAuthUserInfo.getEmail());
        Member member;
        Optional<Member> optionalMember = memberRepository.findByEmail(oAuthUserInfo.getEmail());
        String registrationId = request.getClientRegistration().getRegistrationId();
        log.info("registrationId = {}", registrationId);

        if(optionalMember.isPresent()){
            member = optionalMember.get();
            if(!member.getPlatform().name().equalsIgnoreCase(request.getClientRegistration().getRegistrationId()))
                throw new RuntimeException("looks like you log in with another platform");

            member = updateMember(member, oAuthUserInfo);
        } else{
            member = registerNewMember(oAuthUserInfo);
        }

        return new MemberDetails(member, oAuth2User.getAttributes());
    }

    private Member registerNewMember(OAuth2UserInfo oAuth2UserInfo)
    {
        Member member = Member.builder()
                .email(oAuth2UserInfo.getEmail())
                .username(oAuth2UserInfo.getName())
                .platform(oAuth2UserInfo.getPlatform())
                .password(UUID.randomUUID().toString())
                .build();

        return memberRepository.save(member);
    }

    private Member updateMember(Member member, OAuth2UserInfo oAuth2UserInfo)
    {
        member.updateMember(oAuth2UserInfo.getName());

        return memberRepository.save(member);
    }
}
