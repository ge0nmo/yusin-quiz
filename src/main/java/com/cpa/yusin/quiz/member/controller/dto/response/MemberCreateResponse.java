package com.cpa.yusin.quiz.member.controller.dto.response;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberCreateResponse
{
    private final long id;
    private final String email;
    private final String username;
    private final Platform platform;
    private final Role role;

    public static MemberCreateResponse toMemberResponse(MemberDomain memberDomain)
    {
        return MemberCreateResponse.builder()
                .id(memberDomain.getId())
                .email(memberDomain.getEmail())
                .username(memberDomain.getUsername())
                .platform(memberDomain.getPlatform())
                .role(memberDomain.getRole())
                .build();
    }
}