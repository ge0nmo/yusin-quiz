package com.cpa.yusin.quiz.member.controller.mapper;

import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberDTO;
import com.cpa.yusin.quiz.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper
{
    public MemberCreateResponse toMemberCreateResponse(Member member)
    {
        if(member == null)
            return null;

        return MemberCreateResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .platform(member.getPlatform())
                .role(member.getRole())
                .build();
    }

    public MemberDTO toMemberDTO(Member member)
    {
        if(member == null)
            return null;

        return MemberDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .username(member.getUsername())
                .platform(member.getPlatform())
                .role(member.getRole())
                .subscriberExpiredAt(member.getSubscriptionExpiredAt() == null
                        ? null : member.getSubscriptionExpiredAt())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
