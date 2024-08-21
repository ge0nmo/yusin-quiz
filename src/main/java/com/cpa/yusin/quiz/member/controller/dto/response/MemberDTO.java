package com.cpa.yusin.quiz.member.controller.dto.response;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class MemberDTO
{
    private final long id;
    private final String email;
    private final String username;
    private final Platform platform;
    private final Role role;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static MemberDTO from(MemberDomain memberDomain)
    {
        return MemberDTO.builder()
                .id(memberDomain.getId())
                .email(memberDomain.getEmail())
                .username(memberDomain.getUsername())
                .platform(memberDomain.getPlatform())
                .role(memberDomain.getRole())
                .createdAt(memberDomain.getCreatedAt())
                .updatedAt(memberDomain.getUpdatedAt())
                .build();
    }
}
