package com.cpa.yusin.quiz.global.security.oauth2;

import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.global.security.oauth2.user.OAuth2UserInfo;
import com.cpa.yusin.quiz.global.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2Service extends DefaultOAuth2UserService
{
    private final MemberRepository memberRepository;
    private final UuidHolder uuidHolder;

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
            throw new GlobalException(ExceptionMessage.INVALID_EMAIL);

        log.info("email = {}", oAuthUserInfo.getEmail());
        Member member;
        Optional<Member> optionalMember = memberRepository.findByEmail(oAuthUserInfo.getEmail());
        String registrationId = request.getClientRegistration().getRegistrationId();
        log.info("registrationId = {}", registrationId);

        if(optionalMember.isPresent()){
            member = optionalMember.get();
            if(!member.getPlatform().name().equalsIgnoreCase(request.getClientRegistration().getRegistrationId()))
                throw new GlobalException(ExceptionMessage.USER_NOT_FOUND);

            member.updateFromOauth2(oAuth2User.getName());
        } else{
            member = registerMember(oAuthUserInfo);
        }

        return new MemberDetails(member, oAuth2User.getAttributes());
    }

    private Member registerMember(OAuth2UserInfo oAuth2UserInfo)
    {
        Member member = Member.fromOAuth2(oAuth2UserInfo, uuidHolder);
        return memberRepository.save(member);
    }
}
