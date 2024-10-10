package com.cpa.yusin.quiz.member.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.global.security.oauth2.user.OAuth2UserInfo;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Member extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member fromHome(MemberCreateRequest request, PasswordEncoder passwordEncoder)
    {
        return Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .platform(Platform.HOME)
                .role(Role.USER)
                .build();
    }

    public static Member fromOAuth2(OAuth2UserInfo oAuth2UserInfo, UuidHolder uuidHolder)
    {
        return Member.builder()
                .email(oAuth2UserInfo.getEmail())
                .password(uuidHolder.getRandom())
                .username(oAuth2UserInfo.getName())
                .platform(oAuth2UserInfo.getPlatform())
                .role(Role.USER)
                .build();
    }

    public void updateFromOauth2(String newUsername) {
        this.username = newUsername;
    }

    public void update(MemberUpdateRequest request)
    {
        this.username = request.getUsername();
    }

    public void validateMember(long memberId, Member member)
    {
        if(member.getId().equals(memberId) || Role.ADMIN.equals(member.getRole())){
            return;
        }

        throw new GlobalException(ExceptionMessage.NO_AUTHORIZATION);
    }

    public void activeSubscription()
    {
        this.role = Role.SUBSCRIBER;
    }
}
