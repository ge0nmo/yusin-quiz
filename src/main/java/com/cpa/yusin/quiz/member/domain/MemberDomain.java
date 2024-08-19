package com.cpa.yusin.quiz.member.domain;

import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.domain.type.SubscribeStatus;
import com.cpa.yusin.quiz.global.security.oauth2.user.OAuth2UserInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@ToString
@Builder
@Getter
public class MemberDomain
{
    private Long id;
    private String email;
    private String password;
    private String username;
    private Platform platform;
    private Role role;
    private SubscribeStatus subscribeStatus;

    public static MemberDomain fromHome(MemberCreateRequest request, PasswordEncoder passwordEncoder)
    {
        return MemberDomain.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .platform(Platform.HOME)
                .subscribeStatus(SubscribeStatus.DEFAULT)
                .role(Role.USER)
                .build();
    }

    public static MemberDomain fromOAuth2(OAuth2UserInfo oAuth2UserInfo, UUID uuid)
    {
        return MemberDomain.builder()
                .email(oAuth2UserInfo.getEmail())
                .password(uuid.toString())
                .username(oAuth2UserInfo.getName())
                .platform(oAuth2UserInfo.getPlatform())
                .role(Role.USER)
                .subscribeStatus(SubscribeStatus.DEFAULT)
                .build();
    }

    public MemberDomain updateFromOauth2(MemberDomain memberDomain, String username)
    {
        return MemberDomain.builder()
                .id(memberDomain.getId())
                .email(memberDomain.getEmail())
                .password(memberDomain.getPassword())
                .username(username)
                .platform(memberDomain.getPlatform())
                .subscribeStatus(memberDomain.getSubscribeStatus())
                .role(memberDomain.getRole())
                .build();
    }
}
